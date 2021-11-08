Vagrant.configure("2") do |config|
  config.vm.box = "bento/ubuntu-20.04"
  config.vm.provider "virtualbox" do |vb|
    vb.memory = "2048"
  end
  config.vm.network "forwarded_port", guest: 5050, host: 5050
  config.vm.provision "shell", inline: <<-SHELL
     sudo apt-get install -y git vim
     sudo add-apt-repository -y ppa:openjdk-r/ppa
     sudo apt-get -y update
     sudo apt-get install -y openjdk-11-jdk

     EASECI_VERSION="0.0.1-SNAPSHOT-all"
     JAR_FILE=/home/vagrant/easeci/easeci-core/build/libs/easeci-core-${EASECI_VERSION}.jar

     echo 'Checking is file exist in path:' $JAR_FILE

     if [ -f "$JAR_FILE" ]; then
        echo "Jar file exists, Ease CI is able to run"
     else
        echo "Cannot localise file ${JAR_FILE}"
        git clone --depth=50 https://github.com/easeci/easeci-core.git easeci/easeci-core-java
        cd easeci/easeci-core
        ./gradlew build --stacktrace
        sudo cp easeci.service /etc/systemd/system
        sudo systemctl daemon-reload
        sudo systemctl start easeci.service
     fi
  SHELL
end
