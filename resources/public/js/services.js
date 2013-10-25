HiresApp.factory('UIService', function($rootScope) {

  return {
  }
});

HiresApp.factory('APIService', function($rootScope, $http, $q){

  listToMap = function(list) {
    var map = {};
    $.each(list, function(i) { map[list[i].id] = list[i]; });
    return map;
  };

  http = function(method, url, data) {
    var deferred = $q.defer();
    $http({
      method: method,
      url: '/api' + url,
      data: (data || {}),
    })
    .success(function(returnedData){
      deferred.resolve(returnedData);
    })
    .error(function(returnedData) {
      console.log('API ERROR: ' + returnedData.error);
      deferred.reject(returnedData);
    });
    return deferred.promise;
  };

  httpGET = function(url) {
    return this.http('GET', url, null);
  };
  httpPOST = function(url, data) {
    return this.http('POST', url, data);
  };
  httpPUT = function(url, data) {
    return this.http('PUT', url, data);
  };
  httpDELETE = function(url, data) {
    return this.http('DELETE', url, data);
  };

  getInterviewers = function(withTaskCount, callback) {
      httpGET('/interviewer/all' + (withTaskCount ? '?task-count=true' : '')).then(function(returnedData) {
        $rootScope.interviewersList = returnedData;
        $rootScope.interviewersMap = listToMap(returnedData);
        if (callback) callback();
      });
  }

  /* functions accessible to controllers returned below */
  return {

    listToMap: function(list) {
      var map = {};
      $.each(list, function(i) { map[list[i].id] = list[i]; });
      return map;
    },

    getInterviewers: function(callback) { getInterviewers(false, callback); },
    getInterviewersWithTaskCount: function(callback) { getInterviewers(true, callback); },


    getInterviewerWithTasks: function(interviewerID, callback) {
      httpGET('/interviewer/?id=' + interviewerID).then(function(returnedData) {
        $rootScope.interviewer = returnedData[0];

        var waitingOn = 2;

        httpGET('/interviewer/complete-tasks?id=' + interviewerID).then(function(completeTasksData) {
          waitingOn --;
          $rootScope.completeTasks = completeTasksData;
          if (completeTasksData.length > 0) $rootScope.interviewer['complete-tasks'] = completeTasksData;
          if ((waitingOn == 0) && callback) callback();
        });
        httpGET('/interviewer/incomplete-tasks?id=' + interviewerID).then(function(incompleteTasksData) {
          waitingOn --;
          $rootScope.incompleteTasks = incompleteTasksData;
          if (incompleteTasksData.length > 0) $rootScope.interviewer['incomplete-tasks'] = incompleteTasksData;
          if ((waitingOn == 0) && callback) callback();
        });
      });
    },

    getApplicantsWithTaskCount: function(callback) {
      httpGET('/applicant/all?task-count=true', {'task-count':true}).then(function(returnedData){
        $rootScope.applicantsList = returnedData;
        $rootScope.applicantsMap = listToMap(returnedData);
        if (callback) callback();
      });
    },

    getApplicantsWithTasks: function(callback) {
      httpGET('/applicant/all').then(function(returnedData) {
        $rootScope.applicantsList = returnedData;
        $rootScope.applicantsMap = listToMap(returnedData);

        /* after getting the applicants list, for each applicant get tasks */
        var waitingOn = 0;

        for (var i=0; i<returnedData.length; i++) {
          httpGET('/applicant/complete-tasks?id=' + returnedData[i].id, returnedData[i].id).then(function(completeTasksData) {
            waitingOn --;
            if (completeTasksData.length > 0) $rootScope.applicantsMap[completeTasksData[0].applicant]['complete-tasks'] = completeTasksData;
            if ((waitingOn == 0) && callback) callback();
          });
          httpGET('/applicant/incomplete-tasks?id=' + returnedData[i].id, returnedData[i].id).then(function(incompleteTasksData) {
            waitingOn --;
            if (incompleteTasksData.length > 0) $rootScope.applicantsMap[incompleteTasksData[0].applicant]['incomplete-tasks'] = incompleteTasksData;
            if ((waitingOn == 0) && callback) callback();
          });
          waitingOn += 2;
        };
      });
    },
    getApplicantWithTasks: function(applicantID, callback) {
      httpGET('/applicant/?id=' + applicantID, applicantID).then(function(returnedData) {
        $rootScope.applicant = returnedData[0];

        var waitingOn = 3;

        httpGET('/applicant/tasks?id=' + applicantID, applicantID).then(function(tasksData) {
          waitingOn --;
          $rootScope.totalTasks = tasksData;
          if (tasksData.length > 0) $rootScope.applicant['total-tasks'] = tasksData;
          if (!waitingOn && callback) callback();
        });
        httpGET('/applicant/complete-tasks?id=' + applicantID, applicantID).then(function(completeTasksData) {
          waitingOn --;
          $rootScope.completeTasks = completeTasksData;
          if (completeTasksData.length > 0) { $rootScope.applicant['complete-tasks'] = completeTasksData; }
          if (!waitingOn && callback) callback();
        });
        httpGET('/applicant/incomplete-tasks?id=' + applicantID, applicantID).then(function(incompleteTasksData) {
          waitingOn --;
          $rootScope.incompleteTasks = incompleteTasksData;
          if (incompleteTasksData.length > 0) $rootScope.applicant['incomplete-tasks'] = incompleteTasksData;
          if (!waitingOn && callback) callback();
        });
      });
    },










  }
});