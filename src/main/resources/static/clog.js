var app = angular.module('clog', ['ngRoute', 'ngResource', 'ui.bootstrap']);

app.directive('dateFormat', function($filter) {
    var dateFilter = $filter('date');

    return {
        require: 'ngModel',
        link: function(scope, ele, attr, ngModelCtrl) {

            ngModelCtrl.$formatters.unshift(function(valueFromModel) {
                // return how data will be shown in input
                return dateFilter(valueFromModel, 'yyyy-MM-dd');
            });

            ngModelCtrl.$parsers.push(function(valueFromInput) {
                // return how data should be stored in model
                return valueFromInput.getTime();
            });

        }
    }
});

