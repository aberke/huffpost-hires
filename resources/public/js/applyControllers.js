function ApplyCntl($scope, $location, APIService) {
	$scope.domain = window.location.origin;

	$scope.application;
	$scope.applyStep;



	$scope.goTo = function(path) {
		console.log('goTo')
		console.log(path);
	}

	$scope.submitOutput = function(output) {
		console.log('TODO: submit output');
		$scope.applyStep = 2;
	};
	$scope.submitCode = function() {

		var files = $('#upload-code')[0].files;
		if (files && files.length) { $scope.application.attachment =  files[0]; }

		console.log($scope.application)
		$scope.applyStep = 3;
	}
	$scope.submitPersonalInfo = function(url) {

		var files = $('#upload-resume')[0].files;
		if (files && files.length) { $scope.application.resume =  files[0]; }

		console.log($scope.application)
		$scope.applyStep = 4;
		APIService.submitApplication(url, $scope.application, function(){
			console.log('submitApplication callback')
		});
	};


	var init = function() {
		$scope.applyStep = 0;
		$('.popover-hover').popover({trigger: 'hover'});	
	}
	init();
}