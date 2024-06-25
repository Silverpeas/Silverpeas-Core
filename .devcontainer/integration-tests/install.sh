#!/usr/bin/env bash

WILDFLY_VERSION=${WILDFLY}
USERNAME="${USER:-"silveruser"}"

if [ "Z${WILDFLY_VERSION}" = "Z" ]; then
  echo "Option wildfly is reqquired!"
  exit 1
fi

echo "Wildfly version to install: ${WILDFLY_VERSION}"
echo "User for which Wildfly will run: ${USERNAME}"

apt-get update && apt-get install -y \
    vim \
    ffmpeg \
    imagemagick \
    ghostscript \
    libreoffice-writer \
    libreoffice-calc \
    libreoffice-impress \
    gpgv \
    bash-completion \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

wget -nc https://www.silverpeas.org/files/swftools-bin-0.9.2.zip \
  && echo 'd40bd091c84bde2872f2733a3c767b3a686c8e8477a3af3a96ef347cf05c5e43 *swftools-bin-0.9.2.zip' | sha256sum - \
  && unzip swftools-bin-0.9.2.zip -d / \
  && rm swftools-bin-0.9.2.zip

# Fetch and install PDF2JSON for integration tests
wget -nc https://www.silverpeas.org/files/pdf2json-bin-0.68.zip \
  && echo 'eec849cdd75224f9d44c0999ed1fbe8764a773d8ab0cf7fff4bf922ab81c9f84 *pdf2json-bin-0.68.zip' | sha256sum - \
  && unzip pdf2json-bin-0.68.zip -d / \
  && rm pdf2json-bin-0.68.zip


# Fetch and install Widfly for integration tests
curl -fsSL -o /tmp/wildfly-${WILDFLY_VERSION}.Final.FOR-TESTS.zip https://www.silverpeas.org/files/wildfly-${WILDFLY_VERSION}.Final.FOR-TESTS.zip \
  && mkdir /opt/wildfly-for-tests \
  && unzip /tmp/wildfly-${WILDFLY_VERSION}.Final.FOR-TESTS.zip -d /opt/wildfly-for-tests/ \
  && chown -R ${USERNAME}:${USERNAME} /opt/wildfly-for-tests/ \
  && sed -i 's/\/home\/miguel\/tmp/\/opt\/wildfly-for-tests/g' /opt/wildfly-for-tests/wildfly-${WILDFLY_VERSION}.Final/standalone/configuration/standalone-full.xml \

export JBOSS_HOME="/opt/wildfly-for-tests/wildfly-${WILDFLY_VERSION}.Final/"

# Generate wildfly start/stop script
mkdir -p /home/${USERNAME}/bin \
  && cat >/home/${USERNAME}/bin/wildfly <<'EOF'
#!/usr/bin/env bash

if [ $# -lt 1 ]; then
  echo "Missing argument: start|stop|status"
fi

WILDFLY_VERSION=26.1.3.Final

WILDFLY_HOME=/opt/wildfly-for-tests/wildfly-${WILDFLY_VERSION}

case "$1" in
  start)
    ${WILDFLY_HOME}/bin/standalone.sh -c standalone-full.xml --debug 5005 &> ~/wildfly.log &
    ;;
  stop)
    ${WILDFLY_HOME}/bin/jboss-cli.sh --connect :shutdown
    ;;
  status)
    netstat -an | grep 9990 | grep LISTEN &> /dev/null
    if [ $? -eq 0 ]; then
      echo "Wildfly is started"
      status=$(${WILDFLY_HOME}/bin/jboss-cli.sh --connect command=':read-attribute(name=server-state)' | grep -oP '(?<="result" => ")[a-z]+')
      test "Z$status" = "Z" && status="not yet running"
      echo "Wildfly is $status"
    else
      echo "Wildfly isn't running"
    fi
    ;;
  *)
    echo "Usage: wildfly start|stop|status"
esac
EOF

echo "PATH=${PATH}:/home/${USERNAME}/bin" >> /home/${USERNAME}/.bashrc
chown -R ${USERNAME}:${USERNAME} /home/${USERNAME}/.bashrc
chown -R ${USERNAME}:${USERNAME} /home/${USERNAME}/bin
chmod +x /home/${USERNAME}/bin/wildfly
