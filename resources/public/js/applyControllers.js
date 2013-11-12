function ApplyCntl($scope, $location, BasicService, APIService) {
	$scope.domain = window.location.origin;

	$scope.applicant;
	$scope.applyStep;



	$scope.goTo = function(path) {
		console.log('goTo')
		console.log(path);
	}

	$scope.submitOutput = function(output) {
		console.log('TODO: submit output');
		$scope.applyStep = 2;
	};


	var init = function() {
		$scope.applyStep = 0;
		$('.popover-hover').popover({trigger: 'hover'});	
	}
	init();
}