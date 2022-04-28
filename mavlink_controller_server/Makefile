update:
	poetry update

install:
	poetry install

run:
	poetry run flask run --no-reload

savereq:
	poetry export --without-hashes -f requirements.txt > requirements.txt

test:
	poetry run pytest --cache-clear --new-first --failed-first --verbose
