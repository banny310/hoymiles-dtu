#!/usr/bin/with-contenv bashio
set +u

if [ -e "/data/options.json" ]; then
  bashio::log.info "/data/options.json exists!"

  export CONFIG_FORCE_mqtt_host=$(bashio::services mqtt "host")
  export CONFIG_FORCE_mqtt_port=$(bashio::services mqtt "port")
  export CONFIG_FORCE_mqtt_username=$(bashio::services mqtt "username")
  export CONFIG_FORCE_mqtt_password=$(bashio::services mqtt "password")
  bashio::log.info "MQTT: host=${CONFIG_FORCE_mqtt_host}, port=${CONFIG_FORCE_mqtt_port}, username=${CONFIG_FORCE_mqtt_username}, password=${CONFIG_FORCE_mqtt_password}"

  export CONFIG_FORCE_dtu_host=$(bashio::config 'dtu.host')
  export CONFIG_FORCE_dtu_port=$(bashio::config 'dtu.port')
  bashio::log.info "DTU: host=${CONFIG_FORCE_dtu_host}, port=${CONFIG_FORCE_dtu_port}"
else
  bashio::log.info "/data/options.json NOT exists!"
fi

bashio::log.info "banny310 - Home Assistant Hoymiles DTU Solar Data Gateway Add-on"
bashio::log.info "Starting..."

java -Dconfig.override_with_env_vars=true -jar /app.jar