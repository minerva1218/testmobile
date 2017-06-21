var path;

var confirmDelete = function(p) {
	path = p;
	$.prompt("確認是否刪除文件？", {
		buttons : {
			"確定" : true,
			"取消" : false
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
					"確定" : true
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

