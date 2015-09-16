/**
 * Created by l.henning on 16.06.2015.
 */
app.factory('UserFactory', function($resource) {
    return $resource('/api/user/:id', { id: '@id' }   );
});

app.controller('users', function($scope, UserFactory, $modal) {
    $scope.users = UserFactory.query();
    $scope.alerts = [];
    // Close alert message
    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

    $scope.openModal = function (user) {
        var modalInstance = $modal.open({
            templateUrl: 'users/userModal.html',
            controller: 'userModal',
            resolve: {
                user: function() {
                    return user;
                },
                users: function () {
                    return $scope.users;
                }
            }
        })
    };

    $scope.removeRecord = function(index) {
        $scope.users[index].$remove(); //remove from Factory
        $scope.users.splice(index,1); // remove from DOM
        $scope.alerts.splice(0, 1);
        $scope.alerts.push({
            type: 'success',
            msg: "User erfolgreich entfernt!"
        });
    }

});

app.controller('userModal', function($scope, $rootScope, $modalInstance, user, UserFactory, users) {
    if (angular.isDefined(user)) {
        $scope.user = user;
        $scope.newuser = false;
    } else {
        $scope.user = new UserFactory;
        $scope.newuser = true;
    }

    $scope.closeModal = function () {
        $modalInstance.close();
    };

    $scope.saveModal = function () {
        $scope.user.$save();
        users.push($scope.user);
        $modalInstance.close();
    }

});
