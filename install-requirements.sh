#!/bin/sh

#  install-requirements.sh
#  
#
#  Created by Biko Pougala on 19/03/2021.
#  
yum update
yum install -y unzip zip 
curl -s "https://get.sdkman.io" | bash
chmod a+x "$HOME/.sdkman/bin/sdkman-init.sh"
source "$HOME/.sdkman/bin/sdkman-init.sh"
curl -O https://download.java.net/java/GA/jdk14/076bab302c7b4508975440c56f6cc26a/36/GPL/openjdk-14_linux-x64_bin.tar.gz
tar xvf openjdk-14_linux-x64_bin.tar.gz
mv jdk-14 /opt/
tee /etc/profile.d/jdk14.sh <<EOF
export JAVA_HOME=/opt/jdk-14
export PATH=\$PATH:\$JAVA_HOME/bin
EOF
source /etc/profile.d/jdk14.sh
echo -e '\0033\0143'
