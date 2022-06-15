#!/bin/bash

# This script requires the following:
# Anypoint Platform Organization ID

# Command should be called as follows:
# ./deploy-logger-only.sh some-org-id-value


echo "Deploying module to Exchange"


if [ "$#" -ne 1 ]
then
  echo "[ERROR] You need to provide your OrgId"
  exit 1
fi
echo "Deploying CloudHub Extension to Exchange"
echo "> OrgId: $1"

# Replacing ORG_ID_TOKEN inside pom.xml with OrgId value provided from command line
echo "Replacing OrgId token..."

echo sed -i.bkp "s/ORG_ID_TOKEN/$1/g" "cc-google-analytics-connector-ga4-raas"/pom.xml
sed -i.bkp "s/ORG_ID_TOKEN/$1/g" "cc-google-analytics-connector-ga4-raas"/pom.xml


# Deploying to Exchange
echo "Deploying to Exchange..."

echo mvn -f "cc-google-analytics-connector-ga4-raas"/pom.xml clean deploy
mvn -f "cc-google-analytics-connector-ga4-raas"/pom.xml clean deploy

if [ $? != 0 ]
then
  echo "[ERROR] Failed deploying module to Exchange"
  exit 1
fi

