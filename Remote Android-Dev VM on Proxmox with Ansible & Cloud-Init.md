# Remote Android-Dev VM on Proxmox with Ansible & Cloud-Init

## Prerequisites

1. **Control node** (your laptop) running Debian/Ubuntu.
2. **Python 3** and **pip** installed.
3. **SSH key** generated (we’ll call it `~/.ssh/id_ed25519_ansible`).

```bash
sudo apt update
sudo apt install -y python3 python3-venv python3-pip sshpass
python3 -m venv ~/ansible-env
source ~/ansible-env/bin/activate
pip install ansible proxmoxer requests
ssh-keygen -t ed25519 -f ~/.ssh/id_ed25519_ansible -N ""
```

4. **Copy your public key** to Proxmox’s root account:
```bash
ssh-copy-id -i ~/.ssh/id_ed25519_ansible.pub root@192.168.0.30
```
ansible-proxmox-android/
├── inventory.yml
├── group_vars/
│   └── all.yml
├── create-vm.yml
└── configure-vm.yml

## 1. Inventory (`inventory.yml`)

```yaml
all:
  hosts:
    proxmox:
      ansible_host: 192.168.0.30
      ansible_user: root
      ansible_ssh_private_key_file: ~/.ssh/id_ed25519_ansible
    android-dev-vm:
      ansible_host: "{{ lookup('env','VM_IP') }}"
      ansible_user: devuser
      ansible_ssh_private_key_file: ~/.ssh/id_ed25519_ansible
```

---

## 2. Global vars (`group_vars/all.yml`)

```yaml
proxmox_user:      root@pam
proxmox_password:  YOUR_ROOT_PASSWORD_OR_VAULT_REF
proxmox_node:      homelab
vm_id:             110
vm_name:           android-dev
vm_cores:          4
vm_memory:         8192
vm_bridge:         vmbr0
vm_disk_size:      50G

```

---

## 3. Create VM & import Cloud-Init image (`create-vm.yml`)

```yaml
---
- name: Create and provision Android-Dev VM on Proxmox
  hosts: proxmox
  connection: ssh
  gather_facts: false
  become: true

  vars:
    cloud_image_url:  https://cdimage.debian.org/images/cloud/bookworm/latest/debian-12-genericcloud-amd64.qcow2
    cloud_image_dest: /var/lib/vz/images/debian-12-genericcloud-amd64.qcow2
    vm_net_ip:        "192.168.1.42/24,gw=192.168.1.1"

  tasks:
    - name: Ensure images dir exists
      file:
        path: /var/lib/vz/images
        state: directory
        mode: '0755'

    - name: Download Debian 12 cloud image if missing
      get_url:
        url:   "{{ cloud_image_url }}"
        dest:  "{{ cloud_image_dest }}"
        mode:  '0644'
        force: no

    - name: Create VM skeleton via API
      delegate_to: localhost
      community.general.proxmox_kvm:
        api_user:       "{{ proxmox_user }}"
        api_password:   "{{ proxmox_password }}"
        api_host:       "{{ hostvars['proxmox'].ansible_host }}"
        api_port:       8006
        validate_certs: false
        node:           "{{ proxmox_node }}"
        vmid:           "{{ vm_id }}"
        name:           "{{ vm_name }}"
        cores:          "{{ vm_cores }}"
        memory:         "{{ vm_memory }}"
        ostype:         l26
        scsihw:         virtio-scsi-pci
        net:
          net0: "virtio,bridge={{ vm_bridge }}"
        boot:           d
        full:           true
        state:          present

    - name: Import cloud image as raw disk
      command: >
        /usr/sbin/qm importdisk {{ vm_id }}
        {{ cloud_image_dest }}
        local
      args:
        creates: "/var/lib/vz/images/{{ vm_id }}/vm-{{ vm_id }}-disk-0.raw"

    - name: Attach raw disk & set as bootdisk
      delegate_to: proxmox
      become: true
      command: >
        qm set {{ vm_id }}
          --scsi0 local:{{ vm_id }}/vm-{{ vm_id }}-disk-0.raw
          --bootdisk scsi0 --boot c

    - name: Configure Cloud-Init drive
      delegate_to: localhost
      community.general.proxmox_kvm:
        api_user:       "{{ proxmox_user }}"
        api_password:   "{{ proxmox_password }}"
        api_host:       "{{ hostvars['proxmox'].ansible_host }}"
        api_port:       8006
        validate_certs: false
        node:           "{{ proxmox_node }}"
        vmid:           "{{ vm_id }}"
        ide:
          ide2: local:cloudinit
        ciuser:     devuser
        cipassword: yourpassword
        ipconfig:
          ipconfig0: "{{ vm_net_ip }}"
        sshkeys:    "{{ lookup('file','~/.ssh/id_ed25519_ansible.pub') }}"
        state:      present

    - name: Start the VM
      delegate_to: localhost
      community.general.proxmox_kvm:
        api_user:       "{{ proxmox_user }}"
        api_password:   "{{ proxmox_password }}"
        api_host:       "{{ hostvars['proxmox'].ansible_host }}"
        api_port:       8006
        validate_certs: false
        node:           "{{ proxmox_node }}"
        vmid:           "{{ vm_id }}"
        state:          started

```

---

## 4. Configure Android-dev stack (`configure-vm.yml`)

```yaml
---
- name: Configure Android development environment
  hosts: android-dev-vm
  become: true

  vars:
    android_sdk_root: /opt/android-sdk

  tasks:
    - name: Install prerequisites
      apt:
        update_cache: yes
        name:
          - openjdk-17-jdk
          - unzip
          - curl
          - openssh-server

    - name: Add SSH key for devuser
      authorized_key:
        user: "{{ ansible_user }}"
        key:  "{{ lookup('file','~/.ssh/id_ed25519_ansible.pub') }}"

    - name: Download & extract Android Studio
      get_url:
        url:  https://dl.google.com/dl/android/studio/ide-zips/2023.3.1.20/android-studio-2023.3.1.20-linux.tar.gz
        dest: /tmp/android-studio.tar.gz
      register: dl_as
    - unarchive:
        src:   /tmp/android-studio.tar.gz
        dest:  /opt/
        remote_src: yes
    - file:
        src:  /opt/android-studio/bin/studio.sh
        dest: /usr/local/bin/android-studio
        state: link

    - name: Install Android SDK tools
      shell: |
        mkdir -p {{ android_sdk_root }}/cmdline-tools
        cd {{ android_sdk_root }}/cmdline-tools
        curl -o tools.zip https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
        unzip -q tools.zip && rm tools.zip
      args:
        creates: "{{ android_sdk_root }}/cmdline-tools/bin/sdkmanager"

    - name: Install SDK packages
      shell: |
        yes | {{ android_sdk_root }}/cmdline-tools/bin/sdkmanager \
          "platform-tools" "platforms;android-33" "emulator" "system-images;android-33;google_apis;x86_64"
      environment:
        ANDROID_SDK_ROOT: "{{ android_sdk_root }}"
      args:
        creates: "{{ android_sdk_root }}/platform-tools/adb"

    - name: Reboot to finish setup
      reboot:
        pre_reboot_delay: 10

```

---

## 5. Run it

```bash
# 1) Create and boot the VM
ansible-playbook -i inventory.yml create-vm.yml

# 2) Capture its IP
export VM_IP=$(ansible-inventory -i inventory.yml --host android-dev-vm | jq -r .ansible_host)

# 3) Configure Android stack
ansible-playbook -i inventory.yml configure-vm.yml

```


