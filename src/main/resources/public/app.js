/**
 * Created by merrillm on 11/20/16.
 */
angular.module('attendApp', [
  'ngRoute', 'ngDialog'
])

.config(function($routeProvider) {
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
})

.controller('studentController', function($scope, ngDialog) {
  $scope.clickToOpen = function () {
    ngDialog.open({ template: 'dialogs/testTemplate.html', className: 'ngdialog-theme-default' });
  };
  $scope.clickToOpen();
})

.controller('teacherController', function($scope) {

})

.controller('successController', function($scope, $location) {
  $scope.confirmationNumber = $location.search().confnum;
  if (!$scope.confirmationNumber || $scope.confirmationNumber === true || $scope.confirmationNumber === false)
    $scope.confirmationNumber = 'Number not found! :(';

})

.controller('failureController', function($scope) {

})

.controller('dashboardController', function($scope, ngDialog) {
  $scope.courses = {
    "3": {name: "CSc 131", section: "1", id: "3", keycode: "aaaa"},
    "1": {name: "Foo Bar", section: "1", id: "1", keycode: "bbbb"},
    "2": {name: "CSc 131", section: "2", id: "2", keycode: "cccc"}
  };

  $scope.openAddCourseDialog = function () {
      ngDialog.open({ template: 'dialogs/addCourseTemplate.html', className: 'ngdialog-theme-default' });
  };
})

.controller('editCourseController', function($scope, $http) {

  $scope.courses[$scope.selectedId].keycode = $scope.courses[$scope.selectedId].keycode ||
    ("0000"+Math.floor(Math.random()*65536).toString(16)).slice(-4);

  $scope.submit = function() {
    $http({
      method: 'POST',
      url: '/editCourse'
    }).then(function successCallback(response) {
      // this callback will be called asynchronously
      // when the response is available
    }, function errorCallback(response) {
      // called asynchronously if an error occurs
      // or server returns response with an error status.
    });
  }
})

.controller('addCourseController', function($scope) {
  $scope.submitCourseAdd = function() {

  }
});