/**
 * Created by l.henning on 16.06.2015.
 */
app.factory('routeNavigation', function($route, $location) {
        var routes = [];
        angular.forEach($route.routes, function (route, path) {
            if (route.name) {
                routes.push({
                    path: path,
                    name: route.name
                });
            }
        });
        return {
            routes: routes,
            activeRoute: function (route) {
                return route.path === $location.path();
            }
        };
    });
