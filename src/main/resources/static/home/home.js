/**
 * Created by l.henning on 16.06.2015.
 */
app.controller('home', function($scope, RequestFactory, $http) {
        $scope.greeting = {id: 'xxx', content: 'Hello World!'}

        $http.get('/api/request/count').
            success(function(data) {
                $scope.countRequests = data;
            });

        $http.get('/api/event/count').
            success(function(data) {
                $scope.countEvents = data;
            });

        $http.get('/api/user/count').
            success(function(data) {
                $scope.countUser = data;
            });

    });