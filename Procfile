web: target/universal/stage/bin/cw -Dhttp.port=${PORT} -Ddb.default.driver=com.mysql.jdbc.Driver -Ddb.default.url=${CLEARDB_DATABASE_URL} -Dapplication.baseUrl=http://${hostname} -Dsmtp.mock=false -Dsmtp.user=${EMAIL_USER} -Dsmtp.password=${EMAIL_PW} -Dslackbot.keys=${SLACKBOT_KEYS} -Dsquare.token=${SQUARE_TOKEN} -Dstripe.apikey=${STRIPE_API_KEY} -Dstripe.publickey=${STRIPE_PUBLIC_KEY} -Devolutionplugin=disabled -Dnewrelic.config.file=${NEWRELIC_CONFIG_PATH}