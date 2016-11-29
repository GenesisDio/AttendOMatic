/**
 * Created by merrillm on 11/20/16.
 */
angular.module('attendApp', [
  'ngRoute', 'ngDialog', 'google-signin'
])

.config(function($routeProvider, GoogleSigninProvider) {
  $routeProvider.when('/student', {
    controller: 'studentController',
    templateUrl: 'templates/studentTemplate.html',
    reloadOnSearch: false
  });

  $routeProvider.when('/teacher', {
    controller: 'teacherController',
    templateUrl: 'templates/teacherTemplate.html',
    reloadOnSearch: false
  });

  $routeProvider.when('/teacher/dashboard', {
    controller: 'dashboardController',
    templateUrl: 'templates/teacherDashboardTemplate.html',
    reloadOnSearch: false
  });

  $routeProvider.when('/teacher/addCourse', {
    controller: 'addCourseController',
    templateUrl: 'templates/addCourseTemplate.html',
    reloadOnSearch: false
  });

  $routeProvider.when('/success', {
    controller: 'successController',
    templateUrl: 'templates/successTemplate.html',
    reloadOnSearch: false
  });

  $routeProvider.when('/failure', {
    controller: 'failureController',
    templateUrl: 'templates/failureTemplate.html',
    reloadOnSearch: false
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
            data: id_token
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

.controller('dashboardController', function($scope, $http, ngDialog, $location, $window) {

  $http({
    method: 'GET',
    url: '/api/courses',
  }).then(function successCallback(response) {
    console.log("Got courses:", response);
    $scope.courses = response.data;
    $scope.selectedCourse = (response.data[$location.search().selectedCourse] || {id:undefined}).id;
  }, function errorCallback(response) {
    console.log(response);
  });

  $scope.update = function(){
    $location.search("selectedCourse", $scope.selectedCourse);
  };

  $scope.openAddCourseDialog = function () {
      ngDialog.open({
        template: 'dialogs/addCourseTemplate.html',
        className: 'ngdialog-theme-default',
        controller: 'addCourseController'
      });
  };
  
  $scope.openEditCourseDialog = function () {
      ngDialog.open({
        template: 'dialogs/editCourseTemplate.html',
        className: 'ngdialog-theme-default',
        controller: 'editCourseController',
        scope: $scope
      });
  };
    
  $scope.openManualEntryDialog = function () {
      ngDialog.open({
        template: 'dialogs/manualEntryTemplate.html',
        className: 'ngdialog-theme-default',
        controller: 'manualEntryController',
        scope: $scope
      });
  };

  $scope.openKeyDialog = function () {
    ngDialog.open({
      template: 'dialogs/startAttendance.html',
      className: 'ngdialog-theme-default',
      controller: 'keyController',
      scope: $scope
    });
  };

  $scope.openSheetTab = function() {
    $window.open($scope.courses[$scope.selectedCourse].sheetUrl);
  }

})
.controller('manualEntryController', function($scope, $route, $http) {
  $scope.submitManualEntry = function() {
    console.log($scope);
    console.log($scope.$parent.selectedCourse);
    console.log($scope.manualForm.date.toISOString());
    $http({
      method: 'POST',
      url: '/api/manual',
      data: {
        studentid: $scope.manualForm.studentid,
        date: $scope.manualForm.date.toISOString().substr(0,10),
        courseid: $scope.$parent.selectedCourse
      }
    }).then(function successCallback(response) {
      $route.reload()
    }, function errorCallback(response) {
      // called asynchronously if an error occurs
      // or server returns response with an error status.
    });
  }
})
.controller('editCourseController', function($scope, $route, $http) {

  $scope.courseToEdit = {
    id: $scope.$parent.courses[$scope.$parent.selectedCourse].id,
    name: $scope.$parent.courses[$scope.$parent.selectedCourse].name,
    section: $scope.$parent.courses[$scope.$parent.selectedCourse].section,
  };

  $scope.submitCourseEdit = function() {
    $http({
      method: 'PUT',
      url: '/api/course',
      data: $scope.courseToEdit
    }).then(function successCallback(response) {
      $scope.closeThisDialog();
      $route.reload()
    }, function errorCallback(response) {
    });
  }
})

.controller('addCourseController', function($scope, $route, $http) {
  $scope.submitCourseAdd = function() {
    $http({
      method: 'POST',
      url: '/api/course',
      data: $scope.courseToAdd
    }).then(function successCallback(response) {
      $scope.closeThisDialog();
      $route.reload()
    }, function errorCallback (response) {
    });
  }
})

.controller('keyController', function($scope, $route, $http) {
  $scope.courseToPut = {
  	id: $scope.$parent.selectedCourse
  };
  
  $scope.keygen = function() {
  	$scope.courseToPut.keycode = ("0000"+Math.floor(Math.random()*65536).toString(16)).slice(-4);
  };

  if ($scope.$parent.courses[$scope.$parent.selectedCourse].dirty)
  	$scope.keygen();
  else
    $scope.courseToPut.keycode = $scope.$parent.courses[$scope.$parent.selectedCourse].keycode;
  
  $scope.submitSaveKeycode = function() {
    $http({
      method: 'POST',
      url: '/api/course/keycode',
      data: $scope.courseToPut
    }).then(function successCallback(response) {
      $scope.closeThisDialog();
      $route.reload()
    }, function errorCallback (response) {
      // called asynchronously if an error occurs
      // or server returns response with an error status.
    });
  }
  
  $scope.submitOpenAttendance = function() {
  	$http({
      method: 'POST',
      url: '/api/course/open',
      data: $scope.courseToPut
    }).then(function successCallback(response) {
      $scope.closeThisDialog();
      $route.reload()
    }, function errorCallback (response) {
      // called asynchronously if an error occurs
      // or server returns response with an error status.
    });
  }
});
var ggggg;