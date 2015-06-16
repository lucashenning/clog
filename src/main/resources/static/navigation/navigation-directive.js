/**
 * Created by l.henning on 16.06.2015.
 */
app.directive('navigation', function (routeNavigation) {
        return {
            restrict: "E",
            replace: true,
            templateUrl: "navigation/navigation-directive.tpl.html",
            controller: function ($scope) {
                $scope.routes = routeNavigation.routes;
                $scope.activeRoute = routeNavigation.activeRoute;
            }
        };
    });