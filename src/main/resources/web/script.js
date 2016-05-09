$(document).ready(function() {
	var log_start = -50;

	(function pollServer() {
		$.getJSON('/api/poll', {start: log_start}, function (response) {
			if(log_start < 0) {
				// clear log filler text on the first time
				$("#log").html("");
			}

			handleLog(response.log);
			setTimeout(pollServer, 1000);
		});
	}());

	$('#input-box').keypress(function (e) {
		if (e.which == 13) {
			sendCommand();
			return false;
		}
	});

	function handleLog(log) {
		log_elem = $("#log");
		at_bottom = log_elem[0].scrollHeight - log_elem[0].scrollTop === log_elem[0].clientHeight;

		var i = log_start < 0 ? 0 : (log_start - (log.start - log.count));
		for(; i < log.count; i++) {
			var line = log.lines[i];
			var elem = $("<div>" + line + "</div>", {class: "log-line"});
			log_elem.append(elem);
		}

		if(at_bottom)
			log_elem.scrollTop(log_elem[0].scrollHeight);
		log_start = log.start;
	}

	function sendCommand() {
		var input_box = $("#input-box");
		var command = input_box.val();
		input_box.val('');
		$("#input-status").show();

		$.getJSON('/api/command', {cmd: command, log: log_start}, function(response) {
			handleLog(response.log);

			$("#input-status").hide();
		});
	}
});