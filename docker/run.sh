#!/usr/bin/with-contenv bashio
set +u

if [ -e "/data/config.json" ]; then
  bashio::log.info "/data/config.json exists!"

  export CONFIG_FORCE_mqtt_host=$(bashio::services mqtt "host")
  export CONFIG_FORCE_mqtt_port=$(bashio::services mqtt "port")
  export CONFIG_FORCE_mqtt_username=$(bashio::services mqtt "username")
  export CONFIG_FORCE_mqtt_password=$(bashio::services mqtt "password")
  bashio::log.info "MQTT: host=${CONFIG_FORCE_mqtt_host}, port=${CONFIG_FORCE_mqtt_port}, username=${CONFIG_FORCE_mqtt_username}, password=${CONFIG_FORCE_mqtt_password}"

  export CONFIG_FORCE_dtu_host=$(bashio::config 'dtu_host')
  export CONFIG_FORCE_dtu_port=$(bashio::config 'dtu_port')
  bashio::log.info "DTU: host=${CONFIG_FORCE_dtu_host}, port=${CONFIG_FORCE_dtu_port}"
fi

bashio::log.blue "banny310 - Home Assistant Hoymiles DTU Solar Data Gateway Add-on"

java -Dconfig.override_with_env_vars=true -jar /app.jar