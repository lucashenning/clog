/**
 * Created by l.henning on 16.06.2015.
 */

app.controller('events', function($scope, $http) {
    $http.get('/api/key/all').
        success(function(data) {
            $scope.events = data;
        });
});