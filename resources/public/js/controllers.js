
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


	$scope.goTo = function(path) {
		console.log('goTo')
		console.log(path);
	}

	$scope.interviewerByName = function(name) {
		$.each($scope.interviewersList, function(){
			if ($(this).name === name) return $(this);
		});
	}

	var init = function() {
		$('.popover-hover').popover({trigger: 'hover'});	
	}
	init();
}
function HomeCntl($scope, APIService){

	$scope.file;

	$scope.setFile = function(fileInput){
		console.log('setFile');
		console.log(fileInput);
		console.log(fileInput.value);


		var fd = new FormData();
		fd.append("Resume", fileInput.value);
		console.log(fd);
		APIService.uploadResume(fd);
	}

	$scope.uploadFile = function(file){
		
	}

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

	$scope.rejectedApplicants = [];

	$scope.stagesWithApplicants = {}; //= {stage_number: {name: [applicants]}};

	$scope.addApplicant = function(new_applicant) {
		$('#newApplicantModal').modal('hide');

		if (new_applicant.stage) { new_applicant.stage = new_applicant.stage.number; }
		new_applicant.phone = BasicService.formatPhonenumber(new_applicant.phone);
		new_applicant.goalie = new_applicant.goalie.id;

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
function ApplicantCntl($scope, $routeParams, $location, APIService, BasicService) {

	$scope.editApplicantInfo = false;
	$scope.new_task = {};
	$scope.edit_task;

	$scope.completeTasks;
	$scope.incompleTasks;
	$scope.totalTasks;

	var disableFailTaskBtn = function() { $('#edit-task-fail-btn').attr("disabled", "disabled"); };
	var enableFailTaskBtn = function() { $('#edit-task-fail-btn').removeAttr('disabled', 'disabled'); };
	var disablePassTaskBtn = function() { $('#edit-task-pass-btn').attr("disabled", "disabled"); };
	var enablePassTaskBtn = function() { $('#edit-task-pass-btn').removeAttr('disabled', 'disabled'); };

	$scope.editTask = function(task) {
		console.log('editTask:');
		console.log(task);
		$scope.edit_task = task;
		$('#editTaskModal').modal('show');

		if (task.completed==1 && task.pass == 0) disableFailTaskBtn();
		if (task.completed == 1 && task.pass == 1) disablePassTaskBtn();
	}
	$scope.failTask = function() {
		console.log('failTask')
		$scope.edit_task.pass = 0;
		$scope.edit_task.completed = 1;
		enablePassTaskBtn();
		disableFailTaskBtn();
	}
	$scope.passTask = function() {
		console.log('passTask')
		$scope.edit_task.pass = 1;
		$scope.edit_task.completed = 1;
		enableFailTaskBtn();
		disablePassTaskBtn();
	}
	$scope.updateTask = function() {
		console.log('updateTask: ');
		console.log($scope.edit_task);

		if ($scope.edit_task.interviewer && (typeof $scope.edit_task.interviewer != 'number')) {
			$scope.edit_task.interviewer = $scope.edit_task.interviewer.id;
		}
		APIService.updateTask($scope.edit_task, function() {
			$('#editTaskModal').modal('hide');
			$('.popover-hover').popover({trigger: 'hover'});
		});
	};
	$scope.deleteTask = function(task) {
		APIService.deleteTask(task.id, function() {
			console.log('deleteTask callback')
		});
	}
	$scope.addTask = function(new_task) {
		if( BasicService.checkInputEmpty([
			'new-task-title', 
			'new-task-interviewer',
			'new-task-date',
			'new-task-feedback-due'
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
		console.log('updateApplicantInfo:');
		console.log($scope.applicant);
		APIService.updateApplicant($scope.applicant, function() {
			console.log('callback');
			$('#updateApplicantInfo-btn').html('<h3>Edit</h3>');
			$scope.editApplicantInfo = false;
		});
	}

	$scope.updateApplicantInfo = function(){
			$scope.editApplicantInfo ? updateApplicantInfoSave() : updateApplicantInfoShow();
	}
	$scope.deleteApplicant = function() {
		APIService.deleteApplicant($scope.applicant.id, function() {
			$location.path('/applicants');
		});
	}	
	var init = function() {
		APIService.getApplicantWithTasks($routeParams.id, function() {
			console.log('applicant:');
			console.log($scope.applicant);
			console.log('tasks');
			console.log($scope.totalTasks);
			console.log($scope.completeTasks);
			console.log($scope.incompleteTasks);
			
			$('.popover-hover').popover({trigger: 'hover'});
		});
		APIService.getInterviewers(function() {
			console.log('Interviewers List:');
			console.log($scope.interviewersList);
			console.log('interviewersMap');
			console.log($scope.interviewersMap);
		});
		APIService.getStages(function(){
			console.log('stages:');
			console.log($scope.stagesList);
		});
	}
	init();


	angular.element(document).ready(function () {
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

		new_interviewer.phone = BasicService.formatPhonenumber(new_interviewer.phone);

		APIService.postNewInterviewer(new_interviewer, function() {
			getInterviewers();
		});
	};

	var getInterviewers = function() {
		APIService.getInterviewersWithMetadata(function() {
			console.log($scope.interviewersList);
			console.log($scope.interviewersMap);
		});
	}

	var init = function() {
		getInterviewers();
	}
	init();
}

function InterviewerCntl($scope, $routeParams, BasicService, APIService) {

	$scope.interviewerID;

	$scope.editInterviewerInfo = false;

	$scope.completeTasks;
	$scope.incompleteTasks;


	$scope.stagesWithApplicants = {}; //= {stage_number: {name: [applicants]}};


	$scope.addApplicant = function(new_applicant) {
		$('#newApplicantModal').modal('hide');

		if (new_applicant.stage) { new_applicant.stage = new_applicant.stage.number; }
		new_applicant.phone = BasicService.formatPhonenumber(new_applicant.phone);
		new_applicant.goalie = new_applicant.goalie.id;

		console.log('new_applicant:');
		console.log(new_applicant);
		
		APIService.postNewApplicant(new_applicant, getApplicants);
		$scope.new_applicant = null;
	}

	var updateInterviewerInfoShow = function(){
		$scope.editInterviewerInfo = true;
		$('#updateInterviewerInfo-btn').html('<h3>Save</h3>');
	}
	var updateInterviewerInfoSave = function() {
		console.log('updateInterviewerInfo:');
		console.log($scope.interviewer);
		APIService.updateInterviewer($scope.interviewer, function() {
			console.log('callback');
			$('#updateInterviewerInfo-btn').html('<h3>Edit</h3>');
			$scope.editInterviewerInfo = false;
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
	var getTasks = function() {

	}
	var getApplicants = function(){
		APIService.getStagesWithApplicants($scope.stagesWithApplicants, $scope.interviewerID);
	}

	var init = function() {
		$scope.interviewerID = $routeParams.id;

		APIService.getInterviewerWithTasks($scope.interviewerID, function() {
			
		});
		APIService.getInterviewers();

		getApplicants();
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