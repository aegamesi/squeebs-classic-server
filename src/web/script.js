$(document).ready(function() {
	var log_start = -20;

	(function pollServer() {
		$.getJSON('/api/poll', {start: log_start}, function (response) {
			$.each(response.lines, function(i, e) {
				var line = $("<div>" + e + "</div>", {class: "log-line"});
				$("#log").append(line);
			});
			log_start = response.start;
			setTimeout(pollServer, 1000);
		});
	}());
});