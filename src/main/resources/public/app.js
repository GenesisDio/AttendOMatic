/**
 * Created by merrillm on 11/20/16.
 */
angular.module('attendApp', [
  'ngRoute', 'ngDialog', 'google-signin'
])

.config(function($routeProvider, GoogleSigninProvider) {
  $routeProvider.when('/student', {
    controller: 'studentController',
    templateUrl: 'templates/studentTemplate.html'
  });

  $routeProvider.when('/teacher', {
    controller: 'teacherController',
    templateUrl: 'templates/teacherTemplate.html'
  });

  $routeProvider.when('/teacher/dashboard', {
    controller: 'dashboardController',
    templateUrl: 'templates/teacherDashboardTemplate.html'
  });

  $routeProvider.when('/teacher/addCourse', {
    controller: 'addCourseController',
    templateUrl: 'templates/addCourseTemplate.html'
  });

  $routeProvider.when('/success', {
    controller: 'successController',
    templateUrl: 'templates/successTemplate.html'
  });

  $routeProvider.when('/failure', {
    controller: 'failureController',
    templateUrl: 'templates/failureTemplate.html'
  });

  $routeProvider.otherwise({
    redirectTo: '/student'
  });

  try {
    GoogleSigninProvider.init({
      client_id: '1075583454957-i9nuqbf9k1sputsch437t59eb7jg457n.apps.googleusercontent.com',
    });
  } catch (err) {
    console.err(err);
  }
})

.controller('loginController', function($scope, $http, $location, GoogleSignin) {
  $scope.googleSignIn = function () {
    try {
      GoogleSignin.signIn().then(function (user) {
        try {
          var profile = GoogleSignin.getBasicProfile();
          var id_token = user.getAuthResponse().id_token;
          console.log(profile, id_token);
          $http({
            method: 'POST',
            url: '/api/teacher/idtok',
            data: { name: profile.name, email: profile.email, id_token: id_token }
          }).then(function successCallback(response) {
            $location.path('/teacher/dashboard');
          }, function errorCallback (response) {
            // called asynchronously if an error occurs
            // or server returns response with an error status.
          });
        } catch (err) {
          console.log(err);
        }
      }, function (err) {
        console.log(err);
      });
      ggggg = GoogleSignin;
    } catch (err) {
      console.log(err);
    }
  };
})

.controller('studentController', function($scope, ngDialog) {
  $scope.clickToOpen = function () {
    ngDialog.open({ template: 'dialogs/testTemplate.html', className: 'ngdialog-theme-default' });
  };
  $scope.clickToOpen();
})

.controller('teacherController', function($scope, GoogleSignin) {
})

.controller('successController', function($scope, $location) {
  $scope.confirmationNumber = $location.search().confnum;
  if (!$scope.confirmationNumber || $scope.confirmationNumber === true || $scope.confirmationNumber === false)
    $scope.confirmationNumber = 'Number not found! :(';

})

.controller('failureController', function($scope) {

})

.controller('dashboardController', function($scope, $http, ngDialog) {

  $http({
    method: 'GET',
    url: '/api/courses',
  }).then(function successCallback(response) {
    console.log("Got courses:", response);
    $scope.courses = response.data;
  }, function errorCallback(response) {
    console.log(response);
  });

  $scope.openAddCourseDialog = function () {
      ngDialog.open({
        template: 'dialogs/addCourseTemplate.html',
        className: 'ngdialog-theme-default',
        controller: 'addCourseController'
      });
  };
  $scope.openManualEntryDialog = function () {
      ngDialog.open({
        template: 'dialogs/manualEntryTemplate.html',
        className: 'ngdialog-theme-default',
        controller: 'manualEntryController'
      });
  };
})
.controller('manualEntryController', function($scope, $http) {

  $scope.courses[$scope.selectedId].keycode = $scope.courses[$scope.selectedId].keycode ||
    ("0000"+Math.floor(Math.random()*65536).toString(16)).slice(-4);

  $scope.submitManualEntry = function() {
    $http({
      method: 'PUT',
      url: '/api/course',
      data: $scope.courses[$scope.selectedId]
    }).then(function successCallback(response) {
      // this callback will be called asynchronously
      // when the response is available
    }, function errorCallback(response) {
      // called asynchronously if an error occurs
      // or server returns response with an error status.
    });
  }
})
.controller('editCourseController', function($scope, $http) {

  $scope.courses[$scope.selectedId].keycode = $scope.courses[$scope.selectedId].keycode ||
    ("0000"+Math.floor(Math.random()*65536).toString(16)).slice(-4);

  $scope.submitCourseEdit = function() {
    $http({
      method: 'PUT',
      url: '/api/course',
      data: $scope.courses[$scope.selectedId]
    }).then(function successCallback(response) {
      // this callback will be called asynchronously
      // when the response is available
    }, function errorCallback(response) {
      // called asynchronously if an error occurs
      // or server returns response with an error status.
    });
  }
})

.controller('addCourseController', function($scope, $http) {
  $scope.submitCourseAdd = function() {
    $http({
      method: 'POST',
      url: '/api/course',
      data: $scope.courseToAdd
    }).then(function successCallback(response) {
      $scope.closeThisDialog();
    }, function errorCallback (response) {
      // called asynchronously if an error occurs
      // or server returns response with an error status.
    });
  }
});
var ggggg;