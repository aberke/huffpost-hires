
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

	$scope.stagesList;
	$scope.stagesMap;


	$scope.interviewerByName = function(name) {
		$.each($scope.interviewersList, function(){
			if ($(this).name === name) return $(this);
		});
	}
	$scope.addTextInput = BasicService.addTextInput;

	var init = function() {
		$('.popover-hover').popover({trigger: 'hover'});	
	}
	init();
}
function HomeCntl($scope, APIService){


	var init = function() {
		console.log('HomeCntl');
	}
	init();
}
function AllApplicantsCntl($scope, $location, APIService, BasicService) {

	$scope.goTo = function(path) {
		console.log('goTo');
		console.log(path);
	};

	$scope.rejectedApplicants;

	$scope.stagesWithApplicants = {}; //= {stage_number: {name: [applicants]}};

	$scope.addApplicant = function(new_applicant) {
		$('#newApplicantModal').modal('hide');

		if (new_applicant.stage) { new_applicant.stage = new_applicant.stage.number; }
		new_applicant.phone = BasicService.formatPhonenumber(new_applicant.phone);
		new_applicant.goalie = new_applicant.goalie.id;

		var files = $('#new-applicant-resume')[0].files;
		if (files && files.length) { new_applicant.resume =  files[0]; }

		console.log('new_applicant:');
		console.log(new_applicant);
		
		APIService.postNewApplicant(new_applicant, function() {
			getApplicants();
		});
		$scope.new_applicant = null;
	}

	var getApplicants = function(){
		APIService.getStagesWithApplicants($scope.stagesWithApplicants);

		APIService.getRejectedApplicants(function() {
			console.log('got applicants rejected:')
			console.log($scope.rejectedApplicants);
		});

		APIService.getAllApplicants(function() {
			console.log('got all applicants:');
			console.log($scope.applicantsList);
		});
	}

	var init = function() {
		console.log($scope.stagesWithApplicants);

		getApplicants();

		APIService.getInterviewers(function() {
			console.log('interviewersMap');
			console.log($scope.interviewersMap);
		});
	}
	init();
}
function ApplicantCntl($scope, $routeParams, $location, APIService, BasicService, TaskService) {

	$scope.editApplicantInfo = false;
	$scope.new_task = {};
	$scope.edit_task;

	$scope.homework;

	$scope.showStage;

	$scope.completeTasks;
	$scope.incompleTasks;
	$scope.totalTasks;

	var spinner;

	$scope.editTask = function(task) { TaskService.editTask(task); }
	$scope.failTask = function() { TaskService.failTask(); }
	$scope.passTask = function() { TaskService.passTask(); }


	var updateHomework = function() {
		if ($scope.homework.reviewer && (typeof $scope.homework.reviewer != 'number')) {
			$scope.homework.reviewer = $scope.homework.reviewer.id;
		};
		APIService.updateHomework($scope.homework, function() {
			APIService.getApplicantHomework($scope.applicant.id);
		});
	};
	var addHomework = function(new_homework) {
		if (!new_homework) return false;
		new_homework.applicant = $scope.applicant.id;

		if (new_homework.reviewer) new_homework.reviewer = new_homework.reviewer.id;
		var files = $('#new-homework-attachment')[0].files;
		if (files && files.length) { new_homework.attachment =  files[0]; }

		console.log('adding Homework: ')
		console.log(new_homework)
		APIService.postNewHomework(new_homework, function() {
			APIService.getApplicantHomework($scope.applicant.id);
		});	
	};
	$scope.saveHomework = function() { $scope.homework ? updateHomework() : addHomework($scope.new_homework); }

	$scope.deleteHomework = function() {
		APIService.deleteHomework($scope.homework.id);
		$scope.homework = null;
	};
	
	$scope.updateTask = function() {
		if ($scope.edit_task.interviewer && (typeof $scope.edit_task.interviewer != 'number')) {
			$scope.edit_task.interviewer = $scope.edit_task.interviewer.id;
		}
		APIService.updateTask($scope.edit_task, function() {
			$('#editTaskModal').modal('hide');
			$('.popover-hover').popover({trigger: 'hover'});
			APIService.getApplicantWithTasks($routeParams.id, function() {
				$('.popover-hover').popover({trigger: 'hover'});
			});
		});
	};
	$scope.deleteTask = function(task) {
		APIService.deleteTask(task.id, function() {
			APIService.getApplicantWithTasks($routeParams.id);
		});
	}
	$scope.addTask = function(new_task) {
		if( BasicService.checkInputEmpty([
			'new-task-title', 
			'new-task-interviewer',
			'new-task-date'
		])) { return false; }

		$('#newTaskModal').modal('hide');

		new_task.applicant = $scope.applicant.id;
		new_task.interviewer = new_task.interviewer.id;

		APIService.postNewTask(new_task);
	}

	var updateApplicantInfoShow = function(){
		$scope.editApplicantInfo = true;
		$('#updateApplicantInfo-btn').html('<h3>Save</h3>');
	}
	var updateApplicantInfoSave = function() {
		spinner.start();
		APIService.updateApplicant($scope.applicant, function() {
			APIService.getApplicantWithTasks($routeParams.id, function() {
				spinner.hide();
				$('#updateApplicantInfo-btn').html('<h3>Edit</h3>');
				$scope.editApplicantInfo = false;
				console.log('updated applicant:')
				console.log($scope.applicant)
			});
		});
	}
	$scope.setShowStage = function(stage) { $scope.showStage = stage; }
	$scope.decrStage = function() {
		$scope.applicant.stage = $scope.applicant.stage - 1;
		$scope.showStage = $scope.applicant.stage;
		updateApplicantInfoSave();
	}
	$scope.incrStage = function() {
		$scope.applicant.stage = $scope.applicant.stage + 1;
		$scope.showStage = $scope.applicant.stage;
		updateApplicantInfoSave();
	}
	$scope.passApplicant = function() {
		$scope.applicant.stage = 100;
		$scope.applicant.completed = 1;
		updateApplicantInfoSave();
	}

	$scope.updateApplicantInfo = function(){
			$scope.editApplicantInfo ? updateApplicantInfoSave() : updateApplicantInfoShow();
	}
	$scope.deleteApplicant = function() {
		APIService.deleteApplicant($scope.applicant.id, function() {
			$location.path('/applicants');
		});
	}	

	$scope.attachApplicantResume = function(fileInput) { $scope.applicant.resume = fileInput.files[0]; }

	var init = function() {
		APIService.getApplicantWithTasks($routeParams.id, function() {
			console.log('applicant:');
			console.log($scope.applicant);
			$scope.showStage = $scope.applicant.stage;
			$('.popover-hover').popover({trigger: 'hover'});
		});
		APIService.getInterviewers(function() {
			console.log('interviewersMap');
			console.log($scope.interviewersMap);
		});
		APIService.getStages();
		spinner = new Spinner($('#applicant-info-well')[0],'purple',100,100);

		APIService.getApplicantHomework($routeParams.id, function() {
			console.log('got applicant homework');
			console.log($scope.homework)
		});
	}
	init();


	angular.element(document).ready(function () {
		BasicService.handleDate('new-phonescreen-date', function(date) {
			$scope.new_task.date = date.toJSON();
		});
		BasicService.handleDate('new-task-date', function(date) {
			$scope.new_task.date = date.toJSON();
		});
		BasicService.handleDate('new-task-feedback-due', function(date) {
			$scope.new_task.feedback_due = date.toJSON();
		});
		BasicService.handleDate('edit-task-date', function(date) {
			$scope.edit_task.date = date.toJSON();
		});
		BasicService.handleDate('edit-task-feedback-due', function(date) {
			$scope.edit_task.feedback_due = date.toJSON();
		});
	});
}

function AllInterviewersCntl($scope, BasicService, APIService) {
	

	$scope.addInterviewer = function(new_interviewer) {
		console.log(new_interviewer)
		$('#newInterviewerModal').modal('hide');


		var files = $('#new-interviewer-pic')[0].files;
		if (files && files.length) { new_interviewer.pic =  files[0]; }


		new_interviewer.phone = BasicService.formatPhonenumber(new_interviewer.phone);

		APIService.postNewInterviewer(new_interviewer, function() {
			getInterviewers();
		});
	};

	var getInterviewers = function() {
		APIService.getInterviewersWithMetadata(function() {
			console.log('interviewersList')
			console.log($scope.interviewersList);
			console.log('interviewersMap')
			console.log($scope.interviewersMap);
		});
	}

	var init = function() {
		getInterviewers();
	}
	init();
}

function InterviewerCntl($scope, $timeout, $routeParams, $location, BasicService, APIService, TaskService) {

	var spinner;

	$scope.interviewerID;

	$scope.editInterviewerInfo = false;
	$scope.editTask = false;

	$scope.completeTasks;
	$scope.incompleteTasks;


	$scope.stagesWithApplicants = {}; //= {stage_number: {name: [applicants]}};


	$scope.addApplicant = function(new_applicant) {
		$('#newApplicantModal').modal('hide');

		if (new_applicant.stage) { new_applicant.stage = new_applicant.stage.number; }
		new_applicant.phone = BasicService.formatPhonenumber(new_applicant.phone);
		new_applicant.goalie = new_applicant.goalie.id;
		
		APIService.postNewApplicant(new_applicant, getApplicants);
		$scope.new_applicant = null;
	}

	var updateInterviewerInfoShow = function(){
		$scope.editInterviewerInfo = true;
		$('#updateInterviewerInfo-btn').html('<h3>Save</h3>');
	}
	var updateInterviewerInfoSave = function() {
		spinner.start();
		APIService.updateInterviewer($scope.interviewer, function() {
			APIService.getInterviewerWithTasks($scope.interviewerID, function() {
				$('#updateInterviewerInfo-btn').html('<h3>Edit</h3>');
				$scope.editInterviewerInfo = false;
				spinner.hide();
			});
		});
	}
	$scope.updateInterviewerInfo = function(){
			$scope.editInterviewerInfo ? updateInterviewerInfoSave() : updateInterviewerInfoShow();
	}
	$scope.deleteInterviewer = function() {
		APIService.deleteInterviewer($scope.interviewer.id, function() {
			$location.path('/interviewers');
		});
	}
	$scope.attachInterviewerPic = function(fileInput) { $scope.interviewer.pic = fileInput.files[0]; }
	
	$scope.editTask = function(task) { TaskService.editTask(task); }
	$scope.failTask = function() { TaskService.failTask(); }
	$scope.passTask = function() { TaskService.passTask(); }
	$scope.updateTask = function() {
		if ($scope.edit_task.interviewer && (typeof $scope.edit_task.interviewer != 'number')) {
			$scope.edit_task.interviewer = $scope.edit_task.interviewer.id;
		}
		APIService.updateTask($scope.edit_task, function() {
			$('#editTaskModal').modal('hide');
			$('.popover-hover').popover({trigger: 'hover'});
			APIService.getInterviewerWithTasks($scope.interviewerID);
		});
	};
	$scope.deleteTask = function(task) {
		APIService.deleteTask(task.id, function() {
			APIService.getInterviewerWithTasks($scope.interviewerID);
		});
	}

	var getApplicants = function(){
		APIService.getStagesWithApplicants($scope.stagesWithApplicants, $scope.interviewerID);
		APIService.getAllApplicants();
	}

	var init = function() {
		$scope.interviewerID = $routeParams.id;

		APIService.getInterviewerWithTasks($scope.interviewerID, function(){
			$timeout(function() { 
				$('.popover-hover').popover({trigger: 'hover'}); 
			}, 1000);
		});
		APIService.getInterviewers();

		getApplicants();

		spinner = new Spinner($('#interviewer-info-well')[0],'purple',100,100);
	}
	init();


	angular.element(document).ready(function () {
		BasicService.handleDate('edit-task-date', function(date) {
			$scope.edit_task.date = date.toJSON();
		});
		BasicService.handleDate('edit-task-feedback-due', function(date) {
			$scope.edit_task.feedback_due = date.toJSON();
		});
	});
}

function AllListingsCntl($scope, APIService, BasicService) {

	$scope.listingsList;

	$scope.addListing = function(new_listing) {

		$('#newListingModal').modal('hide');
		console.log(new_listing)

		new_listing.hiring_manager  = new_listing.hiring_manager.id;

		new_listing.responsibilities = [];
		$.each($('#new-listing-responsibilities input'), function(i, object) {
			var val = object.value;
			if (val && val != undefined && val != '') { new_listing.responsibilities.push(val); }
		});
		new_listing.requirements = [];
		$.each($('#new-listing-requirements input'), function(i, object) {
			var val = object.value;
			if (val && val != undefined && val != '') { new_listing.requirements.push(val); }
		});
		console.log(new_listing)

		APIService.postNewListing(new_listing, function(returnedData) {
			var listing = eval("(" + returnedData + ")");
			$scope.listingsList.push(listing);
			$scope.$apply();

			var listing_id = listing.id;

			for (var i=0; i<new_listing.responsibilities.length; i++) {
				APIService.postNewResponsibility({'listing': listing_id, 'title': new_listing.responsibilities[i]});
			};
			for (var i=0; i<new_listing.requirements.length; i++) {
				APIService.postNewRequirement({'listing': listing_id, 'title': new_listing.requirements[i]});
			};
			$scope.new_listing = null;
		});
		/* Reset responsibilities and requirements input lists to only have 2 inputs if inputs were previously added */
		BasicService.limitChildren('#new-listing-responsibilities input', 2);
		BasicService.limitChildren('#new-listing-requirements input', 2);
	}
	var init = function() {
		APIService.getAllListings();
		APIService.getInterviewers();
	};
	init();
};
function ListingCntl($scope, $location, $routeParams, APIService, BasicService) {
	var spinner;
	$scope.listing;
	$scope.responsibilitiesList;
	$scope.requirementsList;

	$scope.saveNewRequirement = function(new_requirement) {
		new_requirement.listing = $scope.listing.id;
		APIService.postNewRequirement(new_requirement, function() {
			APIService.getRequirements($scope.listing.id);
		});
		$scope.new_requirement = null;
	}
	$scope.saveNewResponsibility = function(new_responsibility) {
		new_responsibility.listing = $scope.listing.id;
		APIService.postNewResponsibility(new_responsibility, function() {
			APIService.getResponsibilities($scope.listing.id);
		});
		$scope.new_responsibility = null;
	}
	$scope.deleteResponsibility = function(responsibilityID) {
		APIService.deleteResponsibility(responsibilityID, function() {
			APIService.getResponsibilities($scope.listing.id);
		});
	};
	$scope.deleteRequirement = function(requirementID) {
		APIService.deleteRequirement(requirementID, function() {
			APIService.getRequirements($scope.listing.id);
		});
	};
	$scope.deleteListing = function() {
		APIService.deleteListing($scope.listing.id, function() {
			$location.path('/listings');
		});
	}	
	$scope.updateInfo = function() {
		spinner.start();
		if ($scope.listing.hiring_manager.id) $scope.listing.hiring_manager = $scope.listing.hiring_manager.id;
		
		APIService.updateListing($scope.listing, function() {
			spinner.hide();
			BasicService.flashMessage();
		});
	}


	var init = function() {

		APIService.getListing($routeParams.id, function() {
			console.log('listing:')
			console.log($scope.listing)
		});
		APIService.getResponsibilities($routeParams.id);
		APIService.getRequirements($routeParams.id);
		APIService.getInterviewers(function() {
			console.log($scope.interviewersMap)
		});
		spinner = new Spinner($('#info-well')[0],'purple',100,100);
	};
	init();
}


