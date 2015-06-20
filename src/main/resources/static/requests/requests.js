/**
 * Created by l.henning on 16.06.2015.
 */


app.factory('RequestFactory', function($resource) {
    return $resource('/api/request/:id', { id: '@id' }, {
        approve: {method: 'POST', params: { approve: true } }
    });
});

app.controller('requests', function($scope, RequestFactory, $modal, $filter) {
    $scope.requests = RequestFactory.query();

    angular.forEach($scope.requests, function (request) {
        var startdateAsString = $filter('date')(request.startDate, "dd.MM.yyyy");
        var enddateAsString = $filter('date')(request.endDate, "dd.MM.yyyy");
        request.daterange = startdateAsString + " - " + enddateAsString;
    });

    $scope.alerts = [];
    // Close alert message
    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

    $scope.openModal = function (request) {
        var modalInstance = $modal.open({
            templateUrl: 'requests/requestModal.html',
            controller: 'requestModal',
            resolve: {
                request: function() {
                    return request;
                },
                requests: function () {
                    return $scope.requests;
                }
            }
        })
    }

    $scope.approve = function (request) {
        request.$approve( {}, function(response) {
            $scope.alerts.splice(0, 1);
            $scope.alerts.push(response);
        });
    }

    $scope.removeRecord = function(index) {
        $scope.requests[index].$remove(); //remove from Factory
        $scope.requests.splice(index,1); // remove from DOM
        $scope.alerts.splice(0, 1);
        $scope.alerts.push({
            type: 'success',
            msg: "Request erfolgreich entfernt!"
        });
    }

});

app.controller('requestModal', function($scope, $rootScope, $modalInstance, request, RequestFactory, requests) {
    if (angular.isDefined(request)) {
        $scope.request = request;
        $scope.newrequest = false;
    } else {
        $scope.request = new RequestFactory;
        $scope.request.initiator = {
            username : $rootScope.username
        }
        $scope.newrequest = true;
    }

    $scope.closeModal = function () {
        $modalInstance.close();
    }

    $scope.saveModal = function () {
        $scope.request.$save();
        requests.push($scope.request);
        $modalInstance.close();
    }

});