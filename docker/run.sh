#!/usr/bin/with-contenv bashio
set +u

HA_CONFIG_PATH="/data/options.json"
APP_CONFIG_PATH="/application.json"
APP_JAR="/app.jar"

if [ -e ${HA_CONFIG_PATH} ]; then
  bashio::log.info "/data/options.json exists!"

  CONFIG_FORCE_mqtt_host=$(bashio::services mqtt "host")
  CONFIG_FORCE_mqtt_port=$(bashio::services mqtt "port")
  CONFIG_FORCE_mqtt_username=$(bashio::services mqtt "username")
  CONFIG_FORCE_mqtt_password=$(bashio::services mqtt "password")
  bashio::log.info "MQTT: host=${CONFIG_FORCE_mqtt_host}, port=${CONFIG_FORCE_mqtt_port}, username=${CONFIG_FORCE_mqtt_username}, password=${CONFIG_FORCE_mqtt_password}"

  bashio::config.require 'dtu.host' "DTU ip is required"
  bashio::config.require 'dtu.port' "DTU port is required"
  CONFIG_FORCE_dtu_host=$(bashio::config 'dtu.host')
  CONFIG_FORCE_dtu_port=$(bashio::config 'dtu.port')
  bashio::log.info "DTU: host=${CONFIG_FORCE_dtu_host}, port=${CONFIG_FORCE_dtu_port}"

  # shellcheck disable=SC2002
  cat ${HA_CONFIG_PATH} \
    | jq --arg mqtt_host "$CONFIG_FORCE_mqtt_host" '.mqtt.host |= $mqtt_host' \
    | jq --arg mqtt_port "$CONFIG_FORCE_mqtt_port" '.mqtt.port |= $mqtt_port' \
    | jq --arg mqtt_username "$CONFIG_FORCE_mqtt_username" '.mqtt.username |= $mqtt_username' \
    | jq --arg mqtt_password "$CONFIG_FORCE_mqtt_password" '.mqtt.password |= $mqtt_password' \
    > ${APP_CONFIG_PATH}

  bashio::log.info "banny310 - Home Assistant Hoymiles DTU Solar Data Gateway Add-on"
  bashio::log.info "Configuration:"
  cat /application.json
  bashio::log.info "Starting..."

  java -Dconfig.override_with_env_vars=true -Dconfig.file=${APP_CONFIG_PATH} -ea:com.hoymiles -jar ${APP_JAR}
else
  bashio::log.fatal "/data/options.json NOT exists!"
fi

