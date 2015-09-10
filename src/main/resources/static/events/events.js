/**
 * Created by l.henning on 16.06.2015.
 */

app.factory('EventFactory', function($resource) {
    return $resource('/api/event/:id', { id: '@id' });
});

app.controller('events', function($scope, $http, EventFactory, ParseLinks) {
    $scope.events = [];
    $scope.page = 1;

    $scope.loadPage = function (page) {
        $scope.page = page;
        $scope.loadAll();
    };

    $scope.loadAll = function () {
        EventFactory.query({page: $scope.page, per_page: 10}, function(result, headers) {
            $scope.links = ParseLinks.parse(headers('link'));
            for (var i = 0; i < result.length; i++) {
                $scope.events.push(result[i]);
            }
        });
    };

    $scope.loadAll();

    $scope.alerts = [];
    // Close alert message
    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

    $scope.decrypt = function (event) {
        $scope.alerts.push({
            msg: "Decrypting... "+event.id,
            type: "warning"
        });
        $http.get('/api/event/decrypt/'+event.id).
            success(function(data) {
                $scope.alerts.splice(0, 1);
                $scope.alerts.push(data);
            }).error(function(data) {
                $scope.alerts.splice(0, 1);
                $scope.alerts.push({
                    msg: "Fehler in der Datenübertragung: "+data.message,
                    type: "danger"
                });
            });
    };

    $scope.decay = function (event) {
        $http.get('/api/event/decay/'+event.id).
            success(function(data) {
                $scope.alerts.splice(0, 1);
                $scope.alerts.push(data);
                event.numberOfDecayedBits++;
                $scope.countVariants(event);
            });
    };

    $scope.recover = function (event) {
        $http.get('/api/event/recover/'+event.id).
            success(function(data) {
                $scope.alerts.push(data);
            });
    };

    $scope.countVariants = function (event) {
        $http.get('/api/event/countvariants/'+event.id).
            success(function(data) {
                event.variants = data;
            });
    }


});