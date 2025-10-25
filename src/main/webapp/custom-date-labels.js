(function() {
	if (zk._p = zkpi('zul.lang.wpd')) try {
		msgzk = { NOT_FOUND: "Não encontrado: ", UNSUPPORTED: "Ainda não suportado: ", FAILED_TO_SEND: "Falha ao enviar pedidos ao servidor.", FAILED_TO_RESPONSE: "O servidor está temporariamente fora de serviço.", TRY_AGAIN: "Gostaria de tentar novamente?", UNSUPPORTED_BROWSER: "Navegador não suportado: ", ILLEGAL_RESPONSE: "Resposta desconhecida enviada pelo servidor. Por favor, recarregue e tente novamente.\n", FAILED_TO_PROCESS: "Falha no processamento", GOTO_ERROR_FIELD: "Clique para voltar a introduzir dados", PLEASE_WAIT: "A processar...", FILE_SIZE: "Tamanho do ficheiro: ", KBYTES: "KB", FAILED_TO_LOAD: "Falha no carregamento", FAILED_TO_LOAD_DETAIL: "Pode ter sido causado por tráfego mau. Pode recarregar esta página e tentar novamente.", CAUSE: "Causa: ", LOADING: "Carregando" }; zk.GROUPING = ",";
		zk.DECIMAL = ",";
		zk.PERCENT = "%";
		zk.MINUS = "-";
		zk.PER_MILL = "‰";
		zk.DOW_1ST = 1;
		zk.ERA = "d.C.";
		zk.YDELTA = 0;
		zk.SDOW = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom'];
		zk.S2DOW = zk.SDOW;
		zk.FDOW = ['segunda-feira', 'terça-feira', 'quarta-feira', 'quinta-feira', 'sexta-feira', 'sábado', 'domingo'];
		zk.SMON = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez'];
		zk.S2MON = zk.SMON;
		zk.FMON = ['janeiro', 'fevereiro', 'março', 'abril', 'maio', 'junho', 'julho', 'agosto', 'setembro', 'outubro', 'novembro', 'dezembro'];
		zk.APM = ['da manhã', 'da tarde'];
		msgzul = { UNKNOWN_TYPE: "Tipo de componente desconhecido: ", DATE_REQUIRED: "Tem de especificar uma data. Formato: ", OUT_OF_RANGE: "Fora do intervalo", NO_AUDIO_SUPPORT: "O seu browser não suporta áudio dinâmico" }; msgzul.YES = "Sim"; msgzul.NO = "Não"; zk.$default(msgzul, {
			VALUE_NOT_MATCHED: 'Só são permitidos valores na lista pendente',
			EMPTY_NOT_ALLOWED: 'Este campo não pode estar vazio ou conter apenas espaços. ',
			INTEGER_REQUIRED: 'Tem de especificar um número inteiro, em vez de {0}.',
			NUMBER_REQUIRED: 'Tem de especificar um número, em vez de {0}.',
			DATE_REQUIRED: 'Tem de especificar uma data, em vez de {0}.\nFormat: {1}. ',
			CANCEL: 'Cancelar',
			NO_POSITIVE_NEGATIVE_ZERO: 'Só é permitido um número positivo',
			NO_POSITIVE_NEGATIVE: 'Só é permitido zero',
			NO_POSITIVE_ZERO: 'Só é permitido um número negativo',
			NO_POSITIVE: 'Só é permitido um número negativo ou zero',
			NO_NEGATIVE_ZERO: 'Só é permitido um número positivo',
			NO_NEGATIVE: 'Só é permitido um número positivo ou zero',
			NO_ZERO: 'Não é permitido um número zero',
			NO_FUTURE_PAST_TODAY: 'Só é permitido vazio',
			NO_FUTURE_PAST: 'Só é permitido hoje',
			NO_FUTURE_TODAY: 'Só é permitida a data no passado',
			NO_FUTURE: 'Só é permitida a data no passado ou hoje',
			NO_PAST_TODAY: 'Só é permitida uma data no futuro',
			NO_PAST: 'Só é permitida uma data no futuro ou hoje',
			NO_TODAY: 'Hoje não é permitido',
			FIRST: 'Primeiro',
			LAST: 'Último',
			PREV: 'Anterior',
			NEXT: 'Próximo',
			GRID_GROUP: 'Agrupar',
			GRID_OTHER: 'Outro',
			GRID_ASC: 'Ordenação ascendente',
			GRID_DESC: 'Ordenação descendente',
			GRID_COLUMNS: 'Colunas',
			GRID_UNGROUP: 'Desagrupar',
			WS_HOME: 'Início',
			WS_PREV: 'Anterior',
			WS_NEXT: 'Próximo',
			WS_END: 'Fim',
			OK: 'OK',
			CANCEL: 'Cancelar',
			YES: 'Sim',
			NO: 'Não',
			RETRY: 'Tentar novamente',
			ABORT: 'Abortar',
			IGNORE: 'Ignorar',
			RELOAD: 'Recarregar',
			UPLOAD_CANCEL: 'Cancelar',
			ILLEGAL_VALUE: 'Valor ilegal'
		});
	} finally { zk.setLoaded(zk._p.n); }
})();
