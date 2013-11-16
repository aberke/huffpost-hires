
HiresApp.config(function($routeProvider, $locationProvider) {	
	/* configure the routes */


	$routeProvider.when('/listings', {
		templateUrl: 'partials/listings-all.html',
		controller: AllListingsCntl
	});
	$routeProvider.when('/listing', {
		templateUrl: 'partials/listing.html',
		controller: ListingCntl
	});

	$routeProvider.when('/applicants', {
		templateUrl: 'partials/applicants-all.html',
		controller: AllApplicantsCntl
	});
	$routeProvider.when('/applicant', {
		templateUrl: 'partials/applicant.html',
		controller: ApplicantCntl
	});


	$routeProvider.when('/interviewers', {
		templateUrl: 'partials/interviewers-all.html',
		controller: AllInterviewersCntl
	});
	$routeProvider.when('/interviewer', {
		templateUrl: 'partials/interviewer.html',
		controller: InterviewerCntl
	});

	$routeProvider.when('/', {
		templateUrl: '/partials/index.html',
		controller: HomeCntl
	});

	$locationProvider.html5Mode(true);
});