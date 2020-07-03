$(document).ready(function() {
	var log_beginning = 0;
	var log_start = -50;
	var load_more = 20;

	var last_index = 0;
	var last_lines = [];

	(function pollServer() {
		$.getJSON('/admin/api/poll', {start: log_start}, function (response) {
			if(log_start < 0) {
				// clear log filler text on the first time
				$("#log_inner").html("");
				log_beginning = response.log.beginning;
			}

			handleLog(response.log);
			setTimeout(pollServer, 1000);
		});
	}());

	$('#input-box').keydown(function (e) {
		if (e.which == 13) {
			sendCommand();
			return false;
		}
		if (e.which == 38) {
			// up arrow
			if (last_index > 0) {
				last_index--;
				$("#input-box").val(last_lines[last_index]);
			}
			e.preventDefault();
		}
		if (e.which == 40) {
			// down arrow
			if (last_index < last_lines.length - 1) {
				last_index++;
				$("#input-box").val(last_lines[last_index]);
			}
		}
	});

	$('#load_more').click(function (e) {
		var start = log_beginning - load_more;
		start = Math.max(0, start);
		$.getJSON('/admin/api/poll', {start: start, end: log_beginning}, function (response) {
			log_beginning = response.log.beginning;
			log_inner_elem = $("#log_inner");
			for(var i = response.log.count - 1; i >= 0; i--) {
				var line = response.log.lines[i];
				var elem = $("<div>" + line + "</div>", {class: "log-line"});
				log_inner_elem.prepend(elem);
			}
		});
	});

	function handleLog(log) {
		log_elem = $("#log");
		log_inner_elem = $("#log_inner");
		at_bottom = log_elem[0].scrollHeight - log_elem[0].scrollTop === log_elem[0].clientHeight;

		var i = log_start < 0 ? 0 : (log_start - (log.start - log.count));
		for(; i < log.count; i++) {
			var line = log.lines[i];
			var elem = $("<div>" + line + "</div>", {class: "log-line"});
			log_inner_elem.append(elem);
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

		last_lines.push(command);
		last_index = last_lines.length;

		$.getJSON('/admin/api/command', {cmd: command, log: log_start}, function(response) {
			handleLog(response.log);

			$("#input-status").hide();
		});
	}
});