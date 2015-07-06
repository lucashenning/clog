/**
 * Created by l.henning on 16.06.2015.
 */

app.controller('events', function($scope, $http) {
    $http.get('/api/key/all').
        success(function(data) {
            $scope.events = data;
        });

    $scope.alerts = [];
    // Close alert message
    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

    $scope.decrypt = function (event) {
        $http.get('/api/key/decrypt/'+event.id).
            success(function(data) {
                $scope.alerts.push(data);
            });
    }

    $scope.decay = function (event) {
        $http.get('/api/key/decay/'+event.id).
            success(function(data) {
                $scope.alerts.push(data);
            });
    }


});