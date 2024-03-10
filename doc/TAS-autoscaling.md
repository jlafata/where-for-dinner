https://docs.vmware.com/en/VMware-Tanzu-Application-Service/4.0/tas-for-vms/autoscaler-using-autoscaler-cli.html

### download cf autoscaling plugin from pivnet

### install the plugin
cf install-plugin ~/Downloads/autoscaler-for-pcf-cliplugin-macosx64-binary-2.0.91

cf enable-autoscaling where-for-dinner-search

cf configure-autoscaling where-for-dinner-search autoscaler.yaml
