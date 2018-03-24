var lib = (function() {
	var AJAX_MAX_TRIES = 8;

	var loaderMask = 0;
	var ajaxIsActive = false;
	var ajaxQueue = [];
	var getXMLHttpCode = null;

	function getXMLHttp() {
		var req = null;

		if (getXMLHttpCode !== null) {
			eval(getXMLHttpCode);
			return req;
		}

		if (window.XMLHttpRequest) {
			getXMLHttpCode = 'req=new XMLHttpRequest()';
			eval(getXMLHttpCode);
		} else if (window.ActiveXObject) {
			var msxmls = ['Msxml2.XMLHTTP.5.0', 'Msxml2.XMLHTTP.4.0', 'Msxml2.XMLHTTP.3.0', 'Msxml2.XMLHTTP', 'Microsoft.XMLHTTP'];

			for (var i = 0; i < msxmls.length; i++) {
				try {
					getXMLHttpCode = "req=new ActiveXObject('" + msxmls[i] + "')";
					eval(getXMLHttpCode);
					break;
				} catch (ex) {
					getXMLHttpCode = null;
				}
			}
		}

		if (req === null || req === false) {
			return null;
		}

		return req;
	}

	return {
		LOADER_MASK_AJAX: 1,
		LOADER_MASK_PRELOAD: 2,

		quoteRegExp: function(str) {
			return (str + '').replace(/([.?*+^$[\]\\(){}|-])/g, "\\$1");
		},

		htmlEscape: function(str) {
			return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
		},

		each: function(list, callback) {
			for (var idx = 0; idx < list.length; idx++) {
				callback(list[idx], idx);
			}
		},

		eachKey: function(object, callback) {
			for (var key in object) {
				if (object.hasOwnProperty(key)) {
					callback(key, object[key]);
				}
			}
		},

		query: function(selectorOrElement) {
			var elements = (typeof(selectorOrElement) == 'string' ? document.querySelectorAll(selectorOrElement) : [selectorOrElement]);

			function makeProperties(name, value) {
				if (typeof(name) == 'object') {
					return name;
				} else {
					var res = {};
					res[name] = value;
					return res;
				}
			};

			var res = {
				list: elements,

				dom: function() {
					return (elements.length == 0 ? null : elements[0]);
				},

				on: function(type, callback) {
					lib.each(elements, function(element) {
						element['on' + type] = callback;
					});

					return res;
				},

				hasClass: function(className) {
					if (elements.length == 0) {
						return false;
					} else {
						return ((' ' + elements[0].className + ' ').match(new RegExp(' ' + lib.quoteRegExp(className) + ' ')) != null);
					}
				},

				addClass: function(className) {
					var classNameRe = new RegExp(' ' + lib.quoteRegExp(className) + ' ');

					lib.each(elements, function(element) {
						if (!(' ' + element.className + ' ').match(classNameRe)) {
							element.className += ' ' + className;
						}
					});

					return res;
				},

				removeClass: function(className) {
					var classNameRe = new RegExp(' ' + lib.quoteRegExp(className) + ' ');

					lib.each(elements, function(element) {
						element.className = (' ' + element.className + ' ')
							.replace(/ /g, '  ')
							.replace(classNameRe, '')
							.replace(/[ ]{2,}/g, ' ')
							.replace(/^[ ]+/, '')
							.replace(/[ ]+$/, '');
					});

					return res;
				},

				addRemoveClass: function(value, className) {
					if (value) {
						return res.addClass(className);
					} else {
						return res.removeClass(className);
					}
				},

				set: function(name, value) {
					var properties = makeProperties(name, value);

					lib.each(elements, function(element) {
						lib.eachKey(properties, function(k, v) {
							element[k] = v;
						});
					});

					return res;
				},

				get: function(name, def) {
					if (elements.length == 0) {
						return (def || null);
					} else {
						return (typeof(elements[0][name]) == 'undefined' ? (def || null) : elements[0][name]);
					}
				},

				setStyle: function(name, value) {
					var properties = makeProperties(name, value);

					lib.each(elements, function(element) {
						lib.eachKey(properties, function(k, v) {
							element.style[k] = v;
						});
					});

					return res;
				},

				getStyle: function(name, def) {
					if (elements.length == 0) {
						return (def || null);
					} else {
						return (typeof(elements[0][name]) == 'undefined' ? (def || null) : elements[0][name]);
					}
				},

				setAttribute: function(name, value) {
					var properties = makeProperties(name, value);

					lib.each(elements, function(element) {
						lib.eachKey(properties, function(k, v) {
							element.setAttribute(k, v);
						});
					});

					return res;
				},

				getAttribute: function(name, def) {
					if (elements.length == 0) {
						return (def || null);
					} else {
						return (typeof(elements[0][name]) == 'undefined' ? (elements[0].getAttribute(name) || def || null) : elements[0][name]);
					}
				},

				exec: function(funcName, args) {
					lib.each(elements, function(element) {
						if (typeof(element[funcName]) == 'function') {
							element[funcName].apply(element, args || []);
						}
					});

					return res;
				},

				hide: function() {
					lib.each(elements, function(element) {
						element.style.display = 'none';
					});

					return res;
				},

				show: function() {
					lib.each(elements, function(element) {
						element.style.display = '';
					});

					return res;
				},

				toggle: function(display) {
					lib.each(elements, function(element) {
						element.style.display = (display ? '' : 'none');
					});

					return res;
				},

				each: function(callback) {
					lib.each(elements, callback);
					return res;
				}
			};

			return res;
		},

		toggleLoader: function(mask, visible) {
			if (visible) {
				if (!(loaderMask & mask)) {
					lib.query('.ajax-loader').show();
				}

				loaderMask |= mask;
			} else {
				loaderMask &= ~mask;

				if (!loaderMask) {
					lib.query('.ajax-loader').hide();
				}
			}
		},

		// url: url (required)
		// method: GET or POST ("POST" by default)
		// data: data to send (null by default)
		// callback: callback function (if no callback is given, request will be not async)
		// contentType: content type when data is not null ("application/json" by default)
		// jsonRequest: true by default
		// jsonResponse: true by default
		ajax: function(opts, tryNum) {
			if (opts.callback && ajaxIsActive && !tryNum) {
				ajaxQueue.push(opts);
				return;
			}

			var data = null;
			var req = getXMLHttp();

			if (req === null) {
				return null;
			}

			if (!tryNum) {
				tryNum = 1;
			}

			if (!ajaxIsActive && opts.callback) {
				ajaxIsActive = true;
				lib.toggleLoader(lib.LOADER_MASK_AJAX, true);
			}

			req.open(opts.method || 'POST', opts.url, opts.callback ? true : false);

			if (typeof(opts.data) != 'undefined') {
				if (typeof(opts.jsonRequest) == 'undefined' || opts.jsonRequest) {
					data = JSON.stringify(opts.data);
				} else {
					data = opts.data;
				}
			}

			try {
				if (window.SERVER_AUTH) {
					req.setRequestHeader('Authorization', window.SERVER_AUTH);
					req.withCredentials = true;
				}

				if (data !== null) {
					req.setRequestHeader('Content-type', opts.contentType || 'application/json');
				}

				req.send(data);
			} catch (ex) {
				if (console && console.log) {
					console.log(ex);
				}

				alert("Ajax error: can't send");
			}

			if (opts.callback) {
				req.onreadystatechange = function() {
					if (req.readyState == 4) {
						if (ajaxQueue.length != 0) {
							lib.ajax(ajaxQueue.shift(), 1);
						} else {
							lib.toggleLoader(lib.LOADER_MASK_AJAX, false);
							ajaxIsActive = false;
						}

						try {
							if (req.status == 200 || req.status == 304) {
								if (typeof(opts.jsonResponse) == 'undefined' || opts.jsonResponse) {
									var data = null;

									try {
										data = JSON.parse(req.responseText);
									} catch (ex) {
										if (console && console.log) {
											console.log('Ajax error: invalid JSON');
											console.log(req.responseText);
										}
									}

									try {
										opts.callback(data);
									} catch (ex) {
										if (console && console.log) {
											console.log(ex);
										}

										alert('Ajax error: exception in callback function (1)');
									}
								} else {
									opts.callback(req.responseText);
								}
							} else {
								opts.callback(null);
							}
						} catch (ex) {
							if (console && console.log) {
								console.log(ex);
							}

							alert('Ajax error: exception in callback function (2)');
						}
					}
				}

				return null;
			}

			try {
				if (req.status != 200 && req.status != 304) {
					if (tryNum < AJAX_MAX_TRIES) {
						lib.ajax(opts, tryNum + 1);
					} else {
						if (console && console.log) {
							console.log(req.status);
							console.log(req.responseText);
						}

						alert('Ajax error: too much tries');
					}
				} else {
					return req.responseText;
				}
			} catch (ex) {
				if (console && console.log) {
					console.log(ex);
				}

				alert('Ajax error: mystic');
			}

			return null;
		}
	};
})();
