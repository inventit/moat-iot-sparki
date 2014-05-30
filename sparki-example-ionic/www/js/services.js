angular.module('sparki-example-ionic.services', [])

.factory('MessagingClientFactory', function($q) {
  console.log("MessagingClientFactory instance is being created.");
  return {
    create: function(config) {
      // Modified by dbaba@Inventit Inc. The following code is originally available at https://github.com/hivemq/hivemq-mqtt-web-client
      /**
       * Copyright 2013 dc-square GmbH
       *
       * Licensed under the Apache License, Version 2.0 (the "License");
       * you may not use this file except in compliance with the License.
       * You may obtain a copy of the License at
       *
       * http://www.apache.org/licenses/LICENSE-2.0
       *
       * Unless required by applicable law or agreed to in writing, software
       * distributed under the License is distributed on an "AS IS" BASIS,
       * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       * See the License for the specific language governing permissions and
       * limitations under the License.
       *
       * @author: Christoph Schäbel
       */
      var websocketclient = {
          'client': null,
          'lastMessageId': 1,
          'lastSubId': 1,
          'subscriptions': [],
          'connected': false,
          'onConnectDelegate': null,
          'onFailDelegate': null,
          'onConnectionLostDelegate': null,
          'onMessageArrivedDelegate': null,

          'connect': function () {

            var deferred = $q.defer();
            var host = config.host;
            var port = parseInt(config.port, 10);
            var clientId = config.clientId;
            var username = config.username;
            var password = config.password;
            var keepAlive = parseInt(config.keepAlive);
            var cleanSession = config.isCleanSession;
            var lwTopic = config.lwTopic;
            var lwQos = parseInt(config.lqQos);
            var lwRetain = config.isLwRetain;
            var lwMessage = config.lwMessage;
            var ssl = config.isSsl;

            // delegate callbacks
            websocketclient.onConnectionLostDelegate = config.onConnectionLostDelegate;
            websocketclient.onMessageArrivedDelegate = config.onMessageArrivedDelegate;

            websocketclient.client = new Messaging.Client(host, port, clientId);
            websocketclient.client.onConnectionLost = websocketclient.onConnectionLost;
            websocketclient.client.onMessageArrived = websocketclient.onMessageArrived;

            var options = {
              timeout: 3,
              keepAliveInterval: keepAlive,
              cleanSession: cleanSession,
              useSSL: ssl,
              onSuccess: function () {
                websocketclient.connected = true;
                console.log("connected");
                deferred.resolve();
              },
              onFailure: function (message) {
                websocketclient.connected = false;
                console.log("error: " + message.errorMessage);
                deferred.reject(message.errorMessage);
              }
            };

            if (username.length > 0) {
                options.userName = username;
            }
            if (password.length > 0) {
                options.password = password;
            }
            if (lwTopic.length > 0) {
                var willmsg = new Messaging.Message(lwMessage);
                willmsg.qos = lwQos;
                willmsg.destinationName = lwTopic;
                willmsg.retained = lwRetain;
                options.willMessage = willmsg;
            }

            websocketclient.client.connect(options);
            return deferred.promise;
          },

          'onConnectionLost': function (responseObject) {
              websocketclient.connected = false;
              if (responseObject.errorCode !== 0) {
                  console.log("onConnectionLost:" + responseObject.errorMessage);
              }

              //Cleanup subscriptions
              websocketclient.subscriptions = [];
              
              if (websocketclient.onConnectionLostDelegate) {
                websocketclient.onConnectionLostDelegate(
                  responseObject.errorCode, responseObject.errorMessage, websocketclient);
              }
          },

          'onMessageArrived': function (message) {
              var subscription = websocketclient.getSubscriptionForTopic(message.destinationName);

              var messageObj = {
                  'topic': message.destinationName,
                  'retained': message.retained,
                  'qos': message.qos,
                  'payload': message.payloadString,
                  'timestamp': moment(),
                  'subscriptionId': subscription.id,
              };

              console.log(messageObj);
              messageObj.id = websocketclient.lastMessageId++;
              if (websocketclient.onMessageArrivedDelegate) {
                websocketclient.onMessageArrivedDelegate(messageObj);
              }
          },

          'disconnect': function () {
              websocketclient.client.disconnect();
          },

          'publish': function (topic, payload, qos, retain) {

              if (!websocketclient.connected) {
                  console.log("Not connected");
                  return false;
              }

              try {
                var string = payload;
                if (typeof payload === 'object') {
                  string = JSON.stringify(payload);
                }
                var message = new Messaging.Message(string);
                message.destinationName = topic;
                message.qos = qos;
                message.retained = retain;
                websocketclient.client.send(message);
              } catch (e) {
                console.log(e);
                throw e;
              }
              return true;
          },

          'subscribe': function (topic, qosNr) {

              if (!websocketclient.connected) {
                  console.log("Not connected");
                  return false;
              }

              if (topic.length < 1) {
                  console.log("Topic cannot be empty");
                  return false;
              }

              if ($.grep(websocketclient.subscriptions, function(s) { return (s.topic === topic)}).length > 0) {
                  console.log('You are already subscribed to this topic');
                  return false;
              }

              websocketclient.client.subscribe(topic, {qos: qosNr, timeout: 15 * 60});

              var subscription = {'topic': topic, 'qos': qosNr};
              subscription.id = lastSubId++;
              websocketclient.subscriptions.push(subscription);
              return true;
          },

          'unsubscribe': function (id) {
              var subs = $.grep(websocketclient.subscriptions, function(s) { return (s.id === id)}); 
              if (subs.length == 0) {
                console.log("Nothing to unsubscribe. id => " + id);
                return false;
              } else {
                subs = subs[0];
              }
              websocketclient.client.unsubscribe(subs.topic);
              websocketclient.subscriptions = $.grep(websocketclient.subscriptions, function (item) {
                  return item.id != id;
              });
              return true;
          },

          'getSubscriptionForTopic': function (topic) {
              var i;
              for (i = 0; i < websocketclient.subscriptions.length; i++) {
                  if (websocketclient.compareTopics(topic, websocketclient.subscriptions[i].topic)) {
                      return websocketclient.subscriptions[i];
                  }
              }
              return false;
          },

          'compareTopics': function (topic, subTopic) {
              var pattern = subTopic.replace("+", "(.+?)").replace("#", "(.*)");
              var regex = new RegExp("^" + pattern + "$");
              return regex.test(topic);
          },
      };

      return websocketclient;      
    }
  };
})

.factory('AuthenticationService', function($http, $q) {
  var instance = {
    login : function(baseUrl, appId, authUserId, authPassword) {
      var deferred = $q.defer();
      var url = baseUrl + '?a=' + appId + '&u=' + authUserId;
      $http({
        method: 'GET',
        url: url
      })
      .then(function(result) {
        console.log("1:Login Failed. : " + JSON.stringify(result));
      }, function(result) {
        if (result.data.status == 401) {
          // Challenge
          console.log("Authentication in progress... : " + JSON.stringify(result));

          var clientNonce = instance.generateClientNonce();
          url += "&c=" + encodeURIComponent(
            instance.digest(authPassword, result.data.serverNonce, clientNonce)) +
            "&n=" + encodeURIComponent(clientNonce) +
            "&e=" + result.data.expireAt;
        } else {
          console.log("2:Login Failed. : " + JSON.stringify(result));
        }
      })
      .then(function() {
        console.log('url => ' + url);
        $http({
            method: 'GET',
            url: url
          })
          .then(function(result) {
            if (result.data.status) {
              console.log("4:Login Failed. : " + JSON.stringify(result));
              deferred.reject();
            } else {
              console.log("Login Successful.");
              deferred.resolve(result.data);
            }
          }, function(result) {
            console.log("3:Login Failed. : " + JSON.stringify(result));
            deferred.reject();
          });
      });
      return deferred.promise;
    },

    digest: function(password, serverNonce, clientNonce) {
      var passwordSha1B64 = CryptoJS.SHA1(password).toString(CryptoJS.enc.Base64);
      console.log(passwordSha1B64, clientNonce + ":" + passwordSha1B64 + ":" + serverNonce);
      var hash = CryptoJS.HmacSHA1(passwordSha1B64, clientNonce + ":" + passwordSha1B64 + ":" + serverNonce);
      return hash.toString(CryptoJS.enc.Base64);
    },

    // For Authentication Challenge
    // http://stackoverflow.com/a/1349426
    generateClientNonce: function() {
      var text = "";
      var alphaNumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
      for (var i = 0; i < 10; i++) {
        text += alphaNumeric.charAt(Math.floor(Math.random() * alphaNumeric.length));
      }
      return text;
    }
  }
  return instance;
});
