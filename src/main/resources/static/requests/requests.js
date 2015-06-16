/**
 * Created by l.henning on 16.06.2015.
 */


app.factory('RequestFactory', function($resource) {
    return $resource('/api/requests/:id'); // Note the full endpoint address
});

app.controller('requests', function($scope, RequestFactory) {
    $scope.requests = RequestFactory.get();
});