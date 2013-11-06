HiresApp.factory('BasicService', function($rootScope) {

  return {

    /* handles checking if given input or select elements are empty and colors with class 'error' if so */
    checkInputEmpty: function(elementIDsList) {
      /* first remove previous errors */
      $('.error').removeClass('error');

      var error = false;
      for(var i=0; i < elementIDsList.length; i++) {
        var elt = $('#' + elementIDsList[i]);
        console.log(elt.val());
        if(!elt.val()) {
          elt.addClass('error');
          error = true;
        }
      }
      return error;
    },

    /* handles setting up date picker for given element */
    handleDate: function(elementID, onChangeDate) {
      console.log(elementID)
      var nowTemp = new Date();
      var now = new Date(nowTemp.getFullYear(), nowTemp.getMonth(), nowTemp.getDate(), 0, 0, 0, 0);

      var datePicker = $('#' + elementID).datepicker({
        onRender: function(date) {
          return date.valueOf() < now.valueOf() ? 'disabled' : ''
        }
      }).on('changeDate', function(ev) {
        datePicker.hide();
        onChangeDate(ev.date);
      }).data('datepicker');
    },

    formatPhonenumber: function(phonenumber) {
      if (phonenumber == undefined || phonenumber == null || phonenumber == '') {
        return '';
      }

      phonenumber = phonenumber.split(/[- ]/).join('');
      if (phonenumber[0] !== '1') phonenumber = '1' + phonenumber;
      return phonenumber;
    },
  }
});

HiresApp.factory('APIService', function($rootScope, $http, $q){

  listToMap = function(list) {
    var map = {};
    $.each(list, function(i) { map[list[i].id] = list[i]; });
    return map;
  };


  xhrRequest = function(method, url, data, callback, onProgress) {
    xhr = new XMLHttpRequest();
    xhr.open(method, '/api' + url, true);

    console.log('httpPUT:');
    console.log(data);

    var form = new FormData();
    $.each(data, function(name) { form.append(name, data[name]); });

    xhr.onload = function(e) {
      console.log('onload with status: ' + xhr.status);
      if (xhr.status === 200) {
        console.log(xhr);
        console.log(xhr.response);
        callback(xhr.response);
      } else {
        console.log('Upload error: ' + xhr.status);
      }
    };
    xhr.onerror = function(e) {
      console.log('XHR error.');
    };
    xhr.upload.onprogress = function(e) {
      console.log('xhr.upload.onprogress:');
      console.log(e);
      var percentLoaded;
      if (e.lengthComputable) {
        percentLoaded = Math.round((e.loaded / e.total) * 100);
        console.log('percentLoaded: ' + percentLoaded);
        //return this_s3upload.onProgress(percentLoaded, (percentLoaded === 100 ? 'Finalizing.' : 'Uploading.'), public_url, file);
      }
    };
    xhr.send(form);
  };
  xhrPUT = function(url, data, callback, onProgress) {
    return this.xhrRequest('PUT', url, data, callback, onprogress);
  };
  xhrPOST = function(url, data, callback, onProgress) {
    return this.xhrRequest('POST', url, data, callback, onprogress);
  };
  http = function(method, url, data) {
    var deferred = $q.defer();
    $http({
      method: method,
      url: '/api' + url,
      data: $.param(data || {}),
      headers: {'Content-Type': 'application/x-www-form-urlencoded'}
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
  httpDELETE = function(url, data) {
    return this.http('DELETE', url, data);
  };

  getInterviewers = function(withTaskCount, callback) {
      httpGET('/interviewer/all' + (withTaskCount ? '?task-count=true' : '')).then(function(returnedData) {
        $rootScope.interviewersList = returnedData;
        $rootScope.interviewersMap = listToMap(returnedData);
        if (callback) callback();
      });
  };
  getApplicantTasks = function(applicantID, callback) {
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
  };
  getApplicantsForStage = function(dictionary, index, goalie) {
    var url = '/stage/applicants?stage=' + index + '&pass=1';
    if (goalie) { url = url + '&goalie=' + goalie; }
      httpGET(url).then(function(data) {
        dictionary[index]['applicants'] = data;
      });
  };

  /* functions accessible to controllers returned below */
  return {

    listToMap: function(list) {
      var map = {};
      $.each(list, function(i) { map[list[i].id] = list[i]; });
      return map;
    },

    uploadInterviewerPic: function(formData, callback) {
      httpUploadFile('PUT', '/interviewer/pic', formData, function(returnedData) {
        console.log('returnedData: ');
        console.log(returnedData);
        if (callback) callback(returnedData);
      });
    },
    uploadApplicantResume: function(formData, callback) {
      httpUploadFile('PUT', '/applicant/resume', formData, function(returnedData) {
        console.log('returnedData: ');
        console.log(returnedData);
        if (callback) callback(returnedData);
      });
    },

    getRejectedApplicants: function(callback) {
      httpGET('/applicant/rejected').then(function(returnedData) {
        $rootScope.rejectedApplicants = returnedData;
        if (callback) callback();
      });
    },
    getAllApplicants: function(callback) {
      httpGET('/applicant/all').then(function(returnedData) {
        $rootScope.applicantsList = returnedData;
        $rootScope.applicantsMap = listToMap(returnedData);
        if (callback) callback();
      });
    },

    getStages: function(callback) {
      httpGET('/stage/all').then(function(returnedData) {
        $rootScope.stagesList = returnedData;
        $rootScope.stagesMap = listToMap(returnedData);
        if (callback) callback();
      });
    },
    getStagesWithApplicants: function(stagesWithApplicants, goalieID) {
      httpGET('/stage/all').then(function(returnedData) {
        $rootScope.stagesList = returnedData;
        for (i=0; i<returnedData.length; i++) {
          stagesWithApplicants[returnedData[i].number] = {'name' : returnedData[i].name};
          getApplicantsForStage(stagesWithApplicants, returnedData[i].number, goalieID);
        }
      });
    },

    getInterviewers: function(callback) { getInterviewers(false, callback); },
    getInterviewersWithTaskCount: function(callback) { getInterviewers(true, callback); },

    getInterviewersWithMetadata: function(callback) {
      httpGET('/interviewer/all?extra-data=true', {'extra-data': true}).then(function(returnedData) {
        $rootScope.interviewersList = returnedData;
        $rootScope.interviewersMap = listToMap(returnedData);
        if (callback) callback();
      });
    },

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
        getApplicantTasks(applicantID, callback);
      });
    },

    /* ************** POST REQUESTS ******************/

    postNewApplicant: function(new_applicant, callback) {
      xhrPOST('/applicant', new_applicant, function(returnedData) {
        console.log('postNewApplicant returned:');
        console.log(returnedData);
        if (callback) callback();
      });
    },
    postNewTask: function(new_task, callback) {
      httpPOST('/task', new_task).then(function(returnedData) {
        console.log('postNewTask');
        console.log(returnedData);
        getApplicantTasks(new_task.applicant, callback);
      });
    },
    postNewInterviewer: function(new_interviewer, callback) {
      httpPOST('/interviewer', new_interviewer).then(function(returnedData) {
        console.log('posted new interviewer:');
        console.log(returnedData);
        if (callback) callback();
      });
    },

    /* ************** DELETE REQUESTS ******************/

    deleteApplicant: function(applicantID, callback) {
      httpDELETE('/applicant', {'id': applicantID}).then(function(returnedData) {
        console.log('deleteApplicant with ID ' + applicantID);
        console.log(returnedData);
        if (callback && (returnedData != "ERROR")) callback();
      });
    },  
    deleteInterviewer: function(interviewerID, callback) {
      httpDELETE('/interviewer', {'id': interviewerID}).then(function(returnedData) {
        console.log('deleteinterviewer with ID ' + interviewerID);
        console.log(returnedData);
        if (callback && (returnedData != "ERROR")) callback();
      });
    },  
    deleteTask: function(taskID, callback) {
      httpDELETE('/task', {'id': taskID}).then(function(returnedData) {
        console.log('deletetask with ID ' + taskID);
        console.log(returnedData);
        if (callback && (returnedData != "ERROR")) callback();
      });
    },



    /* ************** PUT REQUESTS ******************/
    updateApplicant: function(applicant, callback) {
      xhrPUT('/applicant', applicant, function(returnedData) {
        console.log('updateApplicant returned data');
        console.log(returnedData);
        if (callback) callback();
      });
    },
    updateInterviewer: function(interviewer, callback) {
      xhrPUT('/interviewer', interviewer, function(returnedData) {
        console.log('updateInterviewer returnedData:')
        console.log(returnedData);
        if (callback) callback();
      });
    },
    updateTask: function(task, callback) {
      xhrPUT('/task', task, function(returnedData) {
        console.log('updateTask returned data:');
        console.log(returnedData);
        getApplicantTasks(task.applicant, callback);
      });
    },


















  }
});