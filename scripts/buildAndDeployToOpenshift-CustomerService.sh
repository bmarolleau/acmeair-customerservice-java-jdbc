#!/bin/bash
# Copyright (c) 2018 IBM Corp.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

MANIFESTS=manifests-openshift

if [[ ${4} == "" ]]
then
  echo "Usage: buildAndDeployToOpenshift.sh  default-route/project_name internal-route/project_name route_host podman|docker [libertyImageName MongoImageName]  "
  exit
fi

IMAGE_PREFIX_EXTERNAL=${1}
IMAGE_PREFIX=${2}
ROUTE_HOST=${3}

if [[ ${4} == "docker" ]]
then
  echo "Using Docker to build/push"
  BUILD_TOOL="docker"
else
  echo "Using podman to build/push"
  BUILD_TOOL="podman"
  TLS_VERIFY="--tls-verify=false"
fi

if [[ ${5} = "" ]]
then
  echo "Using Default Liberty image open-liberty:full"
  LIBERTY_IMAGE="open-liberty:full"
else
  echo "Using App Server image: $5 "
  LIBERTY_IMAGE=$5
fi

#if [[ ${6} = "" ]]
#then
#  echo "Using Default MongoDB image mongo:latest"
#  MONGO_IMAGE="mongo:latest"
#else
#  echo "Using App Server image: $6 "
#  MONGO_IMAGE=$6
#fi

echo "Image Prefix External=${IMAGE_PREFIX_EXTERNAL}"
echo "Image Prefix Internal=${IMAGE_PREFIX}"
echo "Route Host=${ROUTE_HOST}"
cd "$(dirname "$0")"
cd ..
cd ../acmeair-customerservice-java

if [[ `grep -c ${LIBERTY_IMAGE} ./Dockerfile` == 0 ]]
then
  echo "Patching Dockerfile : ${LIBERTY_IMAGE}"
  sed -i.bak "s@open-liberty:full@${LIBERTY_IMAGE}@" ./Dockerfile
fi

#if [[ `grep -c ${MONGO_IMAGE}/a ${MANIFESTS}/deploy-acmeair-customerservice-java.yaml` == 0 ]]
#then
#  echo "Adding ${IMAGE_PREFIX}/"
#  sed -i.bak "s@acmeair-customerservice-java:latest@${IMAGE_PREFIX}/acmeair-customerservice-java:latest@" ${MANIFESTS}/deploy-acmeair-customerservice-java.yaml
#fi

kubectl delete -f ${MANIFESTS}
mvn clean package
${BUILD_TOOL} build --pull -t ${IMAGE_PREFIX_EXTERNAL}/acmeair-customerservice-java --no-cache .
${BUILD_TOOL} push ${IMAGE_PREFIX_EXTERNAL}/acmeair-customerservice-java:latest ${TLS_VERIFY}

echo "Removing  ${LIBERTY_IMAGE} from Dockerfile"
sed -i.bak "s@${LIBERTY_IMAGE}@open-liberty:full@" ./Dockerfile
rm Dockerfile.bak

if [[ `grep -c ${IMAGE_PREFIX}/a ${MANIFESTS}/deploy-acmeair-customerservice-java.yaml` == 0 ]]
then
  echo "Adding ${IMAGE_PREFIX}/"
  sed -i.bak "s@acmeair-customerservice-java:latest@${IMAGE_PREFIX}/acmeair-customerservice-java:latest@" ${MANIFESTS}/deploy-acmeair-customerservice-java.yaml
fi

if [[ `grep -c ${ROUTE_HOST} ${MANIFESTS}/acmeair-customerservice-route.yaml` == 0 ]]
then
  echo "Patching Route Host: ${ROUTE_HOST}"
  sed -i.bak "s@_HOST_@${ROUTE_HOST}@" ${MANIFESTS}/acmeair-customerservice-route.yaml
fi

 if [[ -z "${DB2FORI_HOSTNAME}" ]]; then
   echo "DB2 For i Hostname not defined (export DB2FORI_HOSTNAME=<value>). Please Fix this or patch the ibmi-database service."
 else
   echo "Patching Service with IBM i domain name (cname not IP): ${DB2FORI_HOSTNAME}"
   sed -i.bak "s@bendemo.10.7.19.71.nip.io@${DB2FORI_HOSTNAME}@" ${MANIFESTS}/deploy-acmeair-customerservice-java.yaml
 fi

 if [[ -z "${DB2FORI_HOSTNAME_ALT}" ]]; then
   echo "DB2 For i Alternate Hostname not defined (export DB2FORI_HOSTNAME_ALT=<value>). Please Fix this or patch the ibmi-database-alt service."
   echo "Simple setup: use same value as DB2FORI_HOSTNAME} "
   echo "Patching Service with ALT IBM i domain name (cname not IP): ${DB2FORI_HOSTNAME}"
   sed -i.bak "s@bendemo2.10.7.19.72.nip.io@${DB2FORI_HOSTNAME}@" ${MANIFESTS}/deploy-acmeair-customerservice-java.yaml
 else
   echo "Alt jdbc / Db2 Mirror setup"
   echo "Patching Service with ALT IBM i domain name (cname not IP): ${DB2FORI_HOSTNAME_ALT}"
   sed -i.bak "s@bendemo2.10.7.19.72.nip.io@${DB2FORI_HOSTNAME_ALT}@" ${MANIFESTS}/deploy-acmeair-customerservice-java.yaml
 fi

# if [[ -z "${DB2FORI_USER}" ]]; then
#   echo "DB2 For i User not defined (export DB2FORI_USER=<value>). Please Fix this or patch the deployment env"
# else
#   echo "Patching Deployment with IBM i user profile (rwx access to the db): ${DB2FORI_USER}"
#   sed -i.bak "s@__user__@${DB2FORI_USER}@" ${MANIFESTS}/deploy-acmeair-customerservice-java.yaml
# fi

# if [[ -z "${DB2FORI_PASSWORD}" ]]; then
#   echo "DB2 For i Password not defined (export DB2FORI_PASSWORD=<value>). Please Fix this or patch the deployment env"
# else
#   echo "Patching Deployment with IBM i user passwd (rwx access to the db): ${DB2FORI_PASSWORD}"
#   sed -i.bak "s@__password__@${DB2FORI_PASSWORD}@" ${MANIFESTS}/deploy-acmeair-customerservice-java.yaml
# fi

# if [[ -z "${DB2FORI_LIBRARIES}" ]]; then
#   echo "DB2 For i jdbc LIBL not defined (export DB2FORI_LIBRARIES=<value>). Please Fix this or patch the deployment env"
# else
#   echo "Patching Deployment with IBM i LIBL (ex: acmeair,*libl ): ${DB2FORI_LIBRARIES}"
#   sed -i.bak "s@__liblist__@${DB2FORI_LIBRARIES}@" ${MANIFESTS}/deploy-acmeair-customerservice-java.yaml
# fi

#kubectl apply -f ${MANIFESTS}
oc apply -f ${MANIFESTS}
echo "Removing ${IMAGE_PREFIX}"
sed -i.bak "s@${IMAGE_PREFIX}/acmeair-customerservice-java:latest@acmeair-customerservice-java:latest@" ${MANIFESTS}/deploy-acmeair-customerservice-java.yaml

echo "Removing ${ROUTE_HOST}"
sed -i.bak "s@${ROUTE_HOST}@_HOST_@" ${MANIFESTS}/acmeair-customerservice-route.yaml

echo "Removing Service target - IBM i external name"
sed -i.bak "s@${DB2FORI_HOSTNAME}@bendemo.10.7.19.71.nip.io@" ${MANIFESTS}/deploy-acmeair-customerservice-java.yaml
sed -i.bak "s@${DB2FORI_HOSTNAME_ALT}@bendemo2.10.7.19.72.nip.io@" ${MANIFESTS}/deploy-acmeair-customerservice-java.yaml

# echo "Removing User Name"
# sed -i.bak "s@${DB2FORI_USER}@__user__@" ${MANIFESTS}/deploy-acmeair-customerservice-java.yaml
# echo "Removing User Name"
# sed -i.bak "s@${DB2FORI_PASSWORD}@__password__@" ${MANIFESTS}/deploy-acmeair-customerservice-java.yaml
# echo "Removing LIBL "
# sed -i.bak "s@${DB2FORI_LIBRARIES}@__liblist__@" ${MANIFESTS}/deploy-acmeair-customerservice-java.yaml

rm ${MANIFESTS}/acmeair-customerservice-route.yaml.bak
rm ${MANIFESTS}/deploy-acmeair-customerservice-java.yaml.bak


