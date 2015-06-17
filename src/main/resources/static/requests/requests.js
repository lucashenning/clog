/**
 * Created by l.henning on 16.06.2015.
 */


app.factory('RequestFactory', function($resource) {
    return $resource('/api/request/:id', { id: '@id' }); // Note the full endpoint address
});

app.controller('requests', function($scope, RequestFactory, $modal) {
    $scope.requests = RequestFactory.query();

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
                }
            }
        })
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

app.controller('requestModal', function($scope, $modalInstance, request, RequestFactory) {
    if (angular.isDefined(request)) {
        $scope.request = request;
        $scope.newrequest = false;
    } else {
        $scope.request = new RequestFactory;
        $scope.newrequest = true;
    }

    $scope.closeModal = function () {
        $modalInstance.close();
    }

    $scope.saveModal = function () {
        $scope.request.$save();
        $modalInstance.close();
    }

});