/**
 * Created by merrillm on 11/20/16.
 */
angular.module('attendApp', [
  'ngRoute'
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

.controller('studentController', function($scope) {

})

.controller('teacherController', function($scope) {

})

.controller('successController', function($scope, $location) {
  $scope.confirmationNumber = $location.search().confnum;
  if (!$scope.confirmationNumber || $scope.confirmationNumber === true || $scope.confirmationNumber === false)
    $scope.confirmationNumber = 'Number not found! :(';

})

.controller('failureController', function($scope) {

});