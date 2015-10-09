/**
 * Created by l.henning on 16.06.2015.
 */

app.factory('ResultFactory', function($resource) {
    return $resource('/api/results/:id', { id: '@id' });
});

app.controller('results', function($scope, $http, ResultFactory, ParseLinks) {
    $scope.results = [];
    $scope.page = 1;

    $scope.loadPage = function (page) {
        $scope.page = page;
        $scope.loadAll();
    };

    $scope.loadAll = function () {
        ResultFactory.query({page: $scope.page, per_page: 10}, function(result, headers) {
            $scope.links = ParseLinks.parse(headers('link'));
            for (var i = 0; i < result.length; i++) {
                $scope.results.push(result[i]);
            }
        });
    };

    $scope.loadAll();

    $scope.alerts = [];
    // Close alert message
    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

    $scope.removeRecord = function(index) {
        $scope.results[index].$remove(); //remove from Factory
        $scope.results.splice(index,1); // remove from DOM
        $scope.alerts.splice(0, 1);
        $scope.alerts.push({
            type: 'success',
            msg: "Result erfolgreich entfernt!"
        });
    };


});