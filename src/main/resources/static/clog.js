var app = angular.module('clog', ['ngRoute', 'ngResource', 'ui.bootstrap', 'infinite-scroll']);

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

app.directive('ngReallyClick', ['$modal', function($modal) {
    var ModalInstanceCtrl = function($scope, $modalInstance) {
        $scope.ok = function() {
            $modalInstance.close();
        };

        $scope.cancel = function() {
            $modalInstance.dismiss('cancel');
        };
    };

    return {
        restrict: 'A',
        scope:{
            ngReallyClick:"&",
            item:"="
        },
        link: function(scope, element, attrs) {
            element.bind('click', function() {
                var message = attrs.ngReallyMessage || "Are you sure ?";
                var modalHtml = '<div class="modal-body">' + message + '</div>';
                modalHtml += '<div class="modal-footer"><button class="btn btn-primary" ng-click="ok()">OK</button><button class="btn btn-warning" ng-click="cancel()">Cancel</button></div>';

                var modalInstance = $modal.open({
                    template: modalHtml,
                    controller: ModalInstanceCtrl
                });

                modalInstance.result.then(function() {
                    scope.ngReallyClick({item:scope.item}); //raise an error : $digest already in progress
                }, function() {
                    //Modal dismissed
                });
                //*/

            });

        }
    }
}]);

app.service('ParseLinks', function () {
    this.parse = function (header) {
        if (header.length == 0) {
            throw new Error("input must not be of zero length");
        }

        // Split parts by comma
        var parts = header.split(',');
        var links = {};
        // Parse each part into a named link
        angular.forEach(parts, function (p) {
            var section = p.split(';');
            if (section.length != 2) {
                throw new Error("section could not be split on ';'");
            }
            var url = section[0].replace(/<(.*)>/, '$1').trim();
            var queryString = {};
            url.replace(
                new RegExp("([^?=&]+)(=([^&]*))?", "g"),
                function($0, $1, $2, $3) { queryString[$1] = $3; }
            );
            var page = queryString['page'];
            if( angular.isString(page) ) {
                page = parseInt(page);
            }
            var name = section[1].replace(/rel="(.*)"/, '$1').trim();
            links[name] = page;
        });

        return links;
    }
});
