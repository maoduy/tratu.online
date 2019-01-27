var wto;

var stopCharacters = [ ' ', '\n', '\r', '\t' ];

var highlightWords = [];

$('textarea').highlightWithinTextarea({
	highlight: [
		highlightWords
    ]
});

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

	// Paragraph change event
	$("#paragraphTextArea").bind('input propertychange', function() {
		clearTimeout(wto);
		wto = setTimeout(function() {
			console.log(this.value);
			checkParagraph();
		}, 1000);
	});

	$(".tableRow").click(function() {
		console.log("test");
		var $row = $(this).closest("tr"); // Find the row
		var $tds = $row.find("td");
		$.each($tds, function() {
			console.log($(this).text());
		});
	});

});

$(function() {
	$('textarea').on(
			'click',
			function() {
				var text = $(this).val();
				var start = $(this)[0].selectionStart;
				var end = $(this)[0].selectionEnd;
				while (start > 0) {
					if (stopCharacters.indexOf(text[start]) == -1) {
						--start;
					} else {
						break;
					}
				}
				;
				++start;
				while (end < text.length) {
					if (stopCharacters.indexOf(text[end]) == -1) {
						++end;
					} else {
						break;
					}
				}
				var currentWord = text.substr(start, end - start).replace(
						/[^0-9a-z]/gi, '');

				if (currentWord != '') {
					var $table = $('#newWordTable');
					var rows = $table.find('tr').get();

					$.each(rows, function(index, row) {
						var relatedWords = $(row).attr('relatedWords');
						if (relatedWords.includes(currentWord)) {
							$(row).prependTo("table > tbody");
						}
					});

					$("#rightColumn").scrollTop(0);
				}
			});

	$("#searchBox").autocomplete(
			{
				source : function(request, response) {
					$("#feedback").html("");
					var search = {}
					search["word"] = request.term;
					search["type"] = $("#dictType").val();
					$.ajax({
						type : "POST",
						contentType : "application/json",
						url : "http://localhost:8080/api/autocomplete",
						dataType : "json",
						data : JSON.stringify(search),
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
					event.preventDefault();
					$("#searchBox").val(ui.item.label);
					$("#feedback").html(ui.item.value);
					addHistory(ui.item.label);
					// return false;
				},
				focus : function(event, ui) {
					event.preventDefault();
					$("#searchBox").val(ui.item.label);
					window.history.pushState("object or string", "Title",
							ui.item.label);
				},
				messages : {
					noResults : '',
					results : function() {
					}
				}
			});
});

var hulla = new hullabaloo();

function deleteHistory(word, type, rowId) {
	var data = {}
	data["word"] = word;
	data["type"] = type;

	$.ajax({
		type : "POST",
		contentType : "application/json",
		url : "/api/delete_history",
		data : JSON.stringify(data),
		dataType : 'json',
		cache : false,
		timeout : 600000,
		success : function(data) {
			$("#" + rowId).remove(); // Remove current row
			// $("#deleteMsg").show().delay(5000).fadeOut();
			hulla.send("Delete word successful!", "success");
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

function addHistory(word) {
	var search = {}
	search["word"] = word;
	search["type"] = 'EN_VI';

	$.ajax({
		type : "POST",
		contentType : "application/json",
		url : "/api/history",
		data : JSON.stringify(search),
		dataType : 'json',
		cache : false,
		timeout : 600000,
		success : function(data) {
			console.log(data);
			hulla.send("Add <b>" + word + "</b> to history successfully!",
					"success");
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

function getMp3LinkAndPlay(word) {
	var search = {}
	search["word"] = word; // $("#speaker").attr('word');
	search["type"] = $("#dictType").val();

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

/*
 * function test(rowId) { $('#' + rowId).css('display','block'); $('#' + rowId +
 * "-meaning").css('display','none'); }
 */

function checkParagraph() {

	if ($("#paragraphTextArea").val().length == 0) {
		return;
	}

	var search = {}
	search["paragraph"] = $("#paragraphTextArea").val();

	$
			.ajax({
				type : "POST",
				contentType : "application/json",
				url : "/api/check-paragraph",
				data : JSON.stringify(search),
				dataType : 'json',
				cache : false,
				timeout : 600000,
				success : function(data) {
					$('#newWordTable').empty();
					$("#unknownWords").empty();
					console.log("SUCCESS : ", data);
					if (data.words.length > 0) {
						highlightWords = [];
						var trHTML = '';
						$
								.each(
										data.words,
										function(i, item) {
											//console.log(item.word);
											highlightWords.push(item.relatedWords);
											trHTML += '<tr relatedWords="'
													+ item.relatedWords
													+ '"><td>'
													+ item.relatedWords
													+ '</td><td id="'
													+ item.word + '-meaning">'
													+ item.meaning + '</td>';
											if (data.loggedIn === true) {
												trHTML += '<td><div onclick="addHistory(\''
														+ item.word
														+ '\'); this.style.display = \'none\';"><i class="fa fa-star" style="float:right; color: red; font-size: 20px"></i></div></td>';
											}
											trHTML += '<td style="display:none" id="'
													+ item.word
													+ '">'
													+ item.sentences + '</td>';
											trHTML += '</tr>';
										});
						console.log(highlightWords);
						$('#newWordTable').find('tr').remove();
						$('#newWordTable').append(trHTML);
					} else {
						$('#newWordTable').html("Can't find any result");
					}

					if (data.unknownWords.length > 0) {
						$.each(data.unknownWords, function(i, item) {
							$("#unknownWords").append(
									"<a href='/english-vietnamese/" + item
											+ "'>" + item + "</a>, ");
						});
						$("#unknownWords").prepend("Unknown works: ");
					}
					
					highlighNewWords();
				},
				error : function(e) {

					var json = "<h4>Ajax Response</h4><pre>" + e.responseText
							+ "</pre>";
					$('#newWords').html(json);

					console.log("ERROR : ", e);
				}
			});
}

function dontKnowWordSubmit() {
	var search = {}
	search["word"] = $("#word").val();
	search["type"] = "EN_VI";
	$.ajax({
		type : "POST",
		contentType : "application/json",
		url : "/api/dont-know-word",
		data : JSON.stringify(search),
		dataType : 'json',
		cache : false,
		timeout : 600000,
		success : function(data) {
			$('#meaning').html(data.meaning);
			$('#iKnow').css("display", "none");
			$('#dontKnow').css("display", "none");
			$('#nextButton').css("display", "inline");
		},
		error : function(e) {
			console.log("ERROR : ", e);
		}
	});
}

function reload() {
	window.location.href = "/test";
}

function highlighNewWords() {
	$('textarea').highlightWithinTextarea({
		highlight: [
			highlightWords
	    ]
	});
}