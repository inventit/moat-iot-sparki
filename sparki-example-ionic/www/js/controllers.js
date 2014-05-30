DEBUG = true;
function debug(str, $scope) {
  if (DEBUG) {
    $scope.debug = str;
    $scope.safeApply();
  }
}

angular.module('sparki-example-ionic.controllers', [])

.controller('DashCtrl', function($scope, $http, MessagingClientFactory, AuthenticationService) {
  $scope.baseUrl = 'https://sandbox.service-sync.com/moat/v1/sys/auth';
  $scope.appId = 'your-applicationId';
  $scope.packageId = "sparki-example";
  $scope.authUserId = 'your-clientId@domainId';
  $scope.domainId = $scope.authUserId.substring($scope.authUserId.indexOf('@') + 1);
  $scope.authPassword = 'your-clientSecret';
  $scope.sparkiDeviceUid = 'your-deviceUid';
  $scope.baseTopic = "/" + $scope.appId + "/" + $scope.packageId + "/SparkiAction/" + $scope.domainId;
  $scope.deviceTopic = $scope.baseTopic + "/" + $scope.sparkiDeviceUid;
  $scope.debug = "";
  $scope.forward = function() {
    debug("forward!", $scope);
    $scope.client.publish($scope.deviceTopic, {
      'control' : 'f'
    }, 0, false);
  };
  $scope.back = function() {
    debug("back!", $scope);
    $scope.client.publish($scope.deviceTopic, {
      'control' : 'b'
    }, 0, false);
  };
  $scope.left = function() {
    debug("left!", $scope);
    $scope.client.publish($scope.deviceTopic, {
      'control' : 'l'
    }, 0, false);
  };
  $scope.right = function() {
    debug("right!", $scope);
    $scope.client.publish($scope.deviceTopic, {
      'control' : 'r'
    }, 0, false);
  };
  $scope.stop = function() {
    debug("stop!", $scope);
    $scope.client.publish($scope.deviceTopic, {
      'control' : 's'
    }, 0, false);
  };
  $scope.beep = function() {
    debug("beep!", $scope);
    $scope.client.publish($scope.deviceTopic, {
      'control' : 'p'
    }, 0, false);
  };
  // https://coderwall.com/p/ngisma
  $scope.safeApply = function(fn) {
    var phase = this.$root.$$phase;
    if(phase == '$apply' || phase == '$digest') {
      if(fn && (typeof(fn) === 'function')) {
        fn();
      }
    } else {
      this.$apply(fn);
    }
  };
  $scope.onLoggedIn = function(session) {
    var authToken = session.accessToken;
    console.log('authToken = ' + authToken);
    var config = {
      host: 'sandbox-ssmb.inventit.io',
      port: '80',
      clientId: 'usr:' + $scope.authUserId,
      username: '?c=Raw',
      password: authToken,
      keepAlive: 4 * 60, // must be less than 5 minutes as the underlying impl sets 5 minutes as timeout.
      isCleanSession: true,
      lwTopic: '',
      lqQos: 0,
      isLwRetain: false,
      lwMessage: '',
      isSsl: false,
      onConnectionLostDelegate : function(errorCode, errorMessage, client) {
        debug(errorCode + ':' + errorMessage + '->reconnecting', $scope);
        client.connect().then(
          function() {
            console.log('Connected!');
            debug('Connected!', $scope);
            $scope.client.subscribe($scope.baseTopic, 0);
          },
          function(message) {
            debug(message, $scope);
          }
        );
      },
      onMessageArrivedDelegate : function(messageObject) {
        debug(messageObject, $scope);
      }
    };
    
    // connecting websocket
    $scope.client = MessagingClientFactory.create(config);
    $scope.client.connect().then(
      function() {
        console.log('Connected!');
        debug('Connected!', $scope);
        $scope.client.subscribe($scope.baseTopic, 0);
      },
      function(message) {
        debug(message, $scope);
        AuthenticationService.login($scope.baseUrl,
          $scope.appId, $scope.authUserId, $scope.authPassword).then(
            $scope.onLoggedIn,
            function() {
              console.log('error');
            }
          );
      }
    );
    console.log('init done.');
  };

  debug('Connecting...', $scope);
  AuthenticationService.login($scope.baseUrl,
    $scope.appId, $scope.authUserId, $scope.authPassword).then(
      $scope.onLoggedIn,
      function() {
        console.log('error');
      }
    );
})

.controller('FriendsCtrl', function($scope, Friends) {
  $scope.friends = Friends.all();
})

.controller('FriendDetailCtrl', function($scope, $stateParams, Friends) {
  $scope.friend = Friends.get($stateParams.friendId);
})

.controller('AccountCtrl', function($scope) {
});
