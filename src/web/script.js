$(document).ready(function() {
	var log_start = -20;

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
		log_start = log.start;
		log_elem = $("#log");
		at_bottom = log_elem[0].scrollHeight - log_elem[0].scrollTop === log_elem[0].clientHeight;

		$.each(log.lines, function(i, e) {
			var line = $("<div>" + e + "</div>", {class: "log-line"});
			log_elem.append(line);
		});

		if(at_bottom)
			log_elem.scrollTop(log_elem[0].scrollHeight);
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