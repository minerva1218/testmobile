var path;

var confirmDelete = function(p) {
	path = p;
	$.prompt("�T�{�O�_�R�����H", {
		buttons : {
			"�T�w" : true,
			"����" : false
		},
		callback : deleteCallback
	});
	return false;
}

function deleteCallback(e, v, m, f) {
	if (v != undefined && v == true) {
		var loc = window.location.pathname + path;
		$.post(loc, {}, function(data) {
			$.prompt(data, {
				buttons : {
					"�T�w" : true
				},
				callback : deletedCallback
			});
		});
	}
	return false;
}

function deletedCallback(e, v, m, f) {
	window.location.reload();
}

