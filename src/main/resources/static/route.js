/**
 * Created by l.henning on 16.06.2015.
 */
app.config(function ($routeProvider) {
        $routeProvider.when("/", {
            templateUrl: "home/home.html",
            name: "Home",
            controller: "home"
        }).when("/requests", {
            templateUrl: "requests/requests.html",
            name: "Requests"
        }).when("/events", {
            templateUrl: "events/events.html",
            name: "Events"
        }).otherwise({
            redirectTo: "/"
        });
    }).run(function ($rootScope, $location, $route) {
        $rootScope.$location = $location;
        $rootScope.$route = $route;
        $rootScope.keys = Object.keys;
    });