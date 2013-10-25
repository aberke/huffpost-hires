
function MainCntl($scope, $location, BasicService, APIService) {
	$scope.domain = window.location.origin;

	$scope.interviewersMap; //= {0:interviewer1,1:interviewer2,2:interviewer3};
	$scope.interviewersList; //= [interviewer1, interviewer2, interviewer3];
	$scope.interviewer;

	$scope.applicantsList;
	$scope.applicantsMap; 
	$scope.applicant;

	$scope.tasksMap;
	$scope.tasksList;

	$scope.addNew = false;

	$scope.interviewerByName = function(name) {
		$.each($scope.interviewersList, function(){
			if ($(this).name === name) return $(this);
		});
	}

	// $scope.showAddNew = function() {
	// 	$scope.addNew = false;
	// 	$('#add-applicant-button').text('+');
	// }
	// $scope.hideAddNew = function() {
	// 	$scope.addNew = true;
	// 	$('#add-applicant-button').text('-');
	// }

	$scope.addNewPressed = function(){ $scope.addNew ? $scope.showAddNew() : $scope.hideAddNew(); }

	var init = function() {
		$('.popover-hover').popover({trigger: 'hover'});	
	}
	init();
}
function HomeCntl($scope){

	var init = function() {
	}
	init();
}
function AllApplicantsCntl($scope, $location, APIService, BasicService) {



	$scope.addApplicant = function(new_applicant) {
		console.log('new_applicant:');
		console.log(new_applicant);
		$('#newApplicantModal').modal('hide');

		new_applicant.phone = BasicService.formatPhonenumber(new_applicant.phone);

		new_applicant.goalie = new_applicant.goalie.id;
		/* TODO -- POST TO SERVER */
		APIService.postNewApplicant(new_applicant, function() {
			console.log('callback for postNewApplicant');
		});

		$scope.applicantsMap[new_applicant.id] = new_applicant;
		$scope.applicantsList.push(new_applicant);

		$scope.new_applicant = null;
	}



	var init = function() {
		APIService.getApplicantsWithTaskCount(function() {
			console.log('applicants list');
			console.log($scope.applicantsList);
		});
		APIService.getInterviewers(function() {
			console.log('interviewersMap');
			console.log($scope.interviewersMap);
		});
	}
	init();
}
function ApplicantCntl($scope, $routeParams, APIService) {

	$scope.editApplicantInfo = false;

	$scope.completeTasks;
	$scope.incompleTasks;
	$scope.totalTasks;

	var updateApplicantInfoShow = function(){
		$scope.editApplicantInfo = true;
		$('#updateApplicantInfo-btn').html('<h3>Save</h3>');
	}
	var updateApplicantInfoSave = function() {
		/* TODO: PUT WITH APISERVICE */
		$scope.editApplicantInfo = false;
		$('#updateApplicantInfo-btn').html('<h3>Edit</h3>');
	}

	$scope.updateApplicantInfo = function(){
		$scope.editApplicantInfo ? updateApplicantInfoSave() : updateApplicantInfoShow();
	}	
	var init = function() {
		APIService.getApplicantWithTasks($routeParams.id, function() {
			console.log('applicant:');
			console.log($scope.applicant);
			console.log('tasks');
			console.log($scope.totalTasks);
			console.log($scope.completeTasks);
			console.log($scope.incompleteTasks);
		});
		APIService.getInterviewers(function() {
			console.log('Interviewers List:');
			console.log($scope.interviewersList);
			console.log('interviewersMap');
			console.log($scope.interviewersMap);
		});
	}
	init();
}

function AllInterviewersCntl($scope, BasicService, APIService) {
	/* ALEX SPANGER EDITS HERE */

	var init = function() {
		APIService.getInterviewersWithTaskCount(function() {
			/* your optional callback here */
		});
	}
	init();
}

function InterviewerCntl($scope, $location) {
	/* ALEX SPANGER EDITS HERE */

	$scope.completeTasks;
	$scope.incompleTasks;

	var init = function() {
		APIService.getInterviewerWithTasks($routeParams.id, function() {
			/* optional callback here */
		});
	}
	init();
}