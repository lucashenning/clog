var app = angular.module('clog', ['ngRoute', 'ngResource', 'ui.bootstrap']);

app.directive('dateformat', function($filter) {
    var dateFilter = $filter('date');

    return {
        require: 'ngModel',
        link: function(scope, ele, attr, ngModelCtrl) {

            ngModelCtrl.$formatters.unshift(function(valueFromModel) {
                // return how data will be shown in input
                return dateFilter(valueFromModel, 'dd.MM.yyyy');
            });

            ngModelCtrl.$parsers.push(function(valueFromInput) {
                // return how data should be stored in model
                return parseDate(valueFromInput,"dd.mm.yyyy");
            });

        }
    }
});

function parseDate(input, format) {
    format = format || 'yyyy-mm-dd'; // default format
    var parts = input.match(/(\d+)/g),
        i = 0, fmt = {};
    // extract date-part indexes from the format
    format.replace(/(yyyy|dd|mm)/g, function(part) { fmt[part] = i++; });

    return new Date(parts[fmt['yyyy']], parts[fmt['mm']]-1, parts[fmt['dd']]);
}
