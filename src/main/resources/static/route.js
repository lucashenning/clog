/**
 * Created by l.henning on 16.06.2015.
 */
app.config(function ($routeProvider, $httpProvider) {
        $routeProvider.when("/", {
            templateUrl: "home/home.html"
        }).when("/login", {
            templateUrl: "login/login.html"
        }).when("/requests", {
            templateUrl: "requests/requests.html"
        }).when("/events", {
            templateUrl: "events/events.html"
        }).when("/users", {
            templateUrl: "users/users.html"
        }).when("/results", {
            templateUrl: "results/results.html"
        }).otherwise({
            redirectTo: "/"
        });

        $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';

    }).run(['$rootScope', '$location', function($rootScope, $location){
        var path = function() { return $location.path();};
        $rootScope.$watch(path, function(newVal, oldVal){
            $rootScope.activetab = newVal; // Speicherung des Active TAB für Navigation
        });
    }]);