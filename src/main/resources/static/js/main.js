$(document).ready(function() {
	$("#search-form").submit(function(event) {
		// stop submit the form, we will post it manually.
		console.log("Submiting...");
		event.preventDefault();

		fire_ajax_submit();
	});

	// Set text for button
	$("#langBtn").text(getProperty($("#dictType").val()));

	// Hide language button
	$("#" + $("#dictType").val()).hide();

});

$(function() {
	$("#searchBox").autocomplete({
		source : function(request, response) {
			$("#feedback").html("");
			$.ajax({
				url : "http://localhost:8080/api/autocomplete",
				dataType : "json",
				data : {
					word : request.term
				},
				success : function(data) {
					response($.map(data, function(item) {
						return {
							label : item.word,
							value : item.meaning
						};

					}))
				}
			});
		},
		minLength : 2,
		select : function(event, ui) {
			$("#searchBox").val(ui.item.label);
			$("#feedback").html(ui.item.value);
			return false;
		}, 
		messages: {
	        noResults: '',
	        results: function() {}
	    }
	});
});

function startAutoComplete() {
	var search = {}
	search["word"] = $("#searchBox").val();

	$("#speaker").prop("disabled", true);

	$.ajax({
		type : "POST",
		contentType : "application/json",
		url : "/api/search",
		data : JSON.stringify(search),
		dataType : 'json',
		cache : false,
		timeout : 600000,
		success : function(data) {
			console.log("SUCCESS : ", data);
		},
		error : function(e) {

			var json = "<h4>Ajax Response</h4><pre>" + e.responseText
					+ "</pre>";
			$('#feedback').html(json);

			console.log("ERROR : ", e);
			$("#btn-search").prop("disabled", false);

		}
	});
}

var isGetMp3Link = false;
var currentWord = "";

var lang = {
	'EN_VI' : 'English - Tiếng Việt',
	'VI_EN' : 'Tiếng Việt - English'
};

var getProperty = function(propertyName) {
	return lang[propertyName];
};

function setDict(dictType) {
	if (dictType === 'VI_EN') {
		window.open("http://localhost:8080/vietnamese-english/", "_self");
	} else if (dictType === 'EN_VI') {
		window.open("http://localhost:8080/english-vietnamese/", "_self");
	}
}

function getMp3LinkAndPlay() {
	var search = {}
	search["word"] = $("#speaker").attr('word');

	// Only request Ajax in case mp3 link wasn't get before
	if (!isGetMp3Link || currentWord != search["word"]) {
		$("#speaker").prop("disabled", true);

		$.ajax({
			type : "POST",
			contentType : "application/json",
			url : "/api/get_mp3",
			data : JSON.stringify(search),
			dataType : 'json',
			cache : false,
			timeout : 600000,
			success : function(data) {
				console.log("SUCCESS : ", data);
				$("#speaker").prop("disabled", false);
				$("#audio").prop("src",
						"https://dictionary.cambridge.org/" + data.mp3Link);
				document.getElementById("audio").play();
				isGetMp3Link = true;
				currentWord = $("#speaker").attr('word');
			},
			error : function(e) {

				var json = "<h4>Ajax Response</h4><pre>" + e.responseText
						+ "</pre>";
				$('#feedback').html(json);

				console.log("ERROR : ", e);
				$("#btn-search").prop("disabled", false);

			}
		});
	} else {
		document.getElementById("audio").play();
	}

}

function fire_ajax_submit() {

	if ($("#searchBox").val().length == 0) {
		return;
	}

	var search = {}
	search["word"] = $("#searchBox").val();
	search["type"] = $("#dictType").val();

	$("#btn-search").prop("disabled", true);

	$.ajax({
		type : "POST",
		contentType : "application/json",
		url : "/api/search",
		data : JSON.stringify(search),
		dataType : 'json',
		cache : false,
		timeout : 600000,
		success : function(data) {
			$("#btn-search").prop("disabled", false);
			console.log("SUCCESS : ", data);
			if (data.result.length > 0) {
				$('#feedback').html(data.result[0].meaning);
				window.history.pushState("object or string", "Title",
						search["word"]);
			} else {
				$('#feedback').html("Can't find " + search["word"]);
			}

		},
		error : function(e) {

			var json = "<h4>Ajax Response</h4><pre>" + e.responseText
					+ "</pre>";
			$('#feedback').html(json);

			console.log("ERROR : ", e);
			$("#btn-search").prop("disabled", false);

		}
	});

}