import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { ConfigurationForm } from '../ConfigurationForm.component';

global.fetch = jest.fn();

describe('ConfigurationForm component', () => {
  test('successfully submitting the form after populating it with correct values', async () => {
    // given
    (fetch as jest.Mock).mockImplementationOnce(
      // React 18 automatically batches setState in async code now, we need to delay resolve to the next tick
      () => new Promise(resolve => setTimeout(() => resolve({ ok: true }), 0))
    );

    const user = userEvent.setup();
    render(
      <BrowserRouter>
        <ConfigurationForm />
      </BrowserRouter>
    );

    // when
    await user.type(screen.getByRole('textbox', { name: /Project name/i }), 'test-project');
    await user.type(screen.getByRole('textbox', { name: /Group ID/i }), 'com.softwaremill');

    await user.click(screen.getByRole('button', { name: /Effect type/i }));
    await user.click(screen.getByText('Future'));

    await user.click(within(screen.getByRole('radiogroup', { name: /Scala version/i })).getByText('2'));

    await user.click(screen.getByRole('button', { name: /Server implementation/i }));
    await user.click(screen.getByText('Netty'));

    await user.click(within(screen.getByRole('radiogroup', { name: /Build tool/i })).getByText('Scala CLI'));

    await user.click(
      within(
        screen.getByRole('radiogroup', {
          name: /Expose endpoint documentation using Swagger UI/i,
        })
      ).getByText('yes')
    );
    await user.click(within(screen.getByRole('radiogroup', { name: /Add JSON endpoint using/i })).getByText('circe'));
    await user.click(within(screen.getByRole('radiogroup', { name: /Add metrics endpoints/i })).getByText('yes'));

    await user.click(screen.getByRole('button', { name: /Generate .zip/i }));

    // then
    await waitFor(() => {
      expect(screen.getByRole('progressbar')).toBeVisible();
    });

    expect(fetch).toHaveBeenCalledTimes(1);
    expect(fetch).toHaveBeenCalledWith('https://adopt-tapir.softwaremill.com/api/v1/starter.zip', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      // NOTE: below order matters as we are comparing strings
      body: JSON.stringify({
        builder: 'ScalaCli',
        addMetrics: true,
        effect: 'FutureEffect',
        json: 'Circe',
        addDocumentation: true,
        implementation: 'Netty',
        scalaVersion: 'Scala2',
        groupId: 'com.softwaremill',
        projectName: 'test-project',
      }),
    });

    await waitFor(() => {
      expect(screen.getByRole('progressbar')).not.toBeVisible();
    });
  });

  test('for validation error and lack of network request while trying to submit not complete form', async () => {
    // given
    const user = userEvent.setup();
    render(
      <BrowserRouter>
        <ConfigurationForm />
      </BrowserRouter>
    );

    // when
    await user.type(screen.getByRole('textbox', { name: /Project name/i }), 'test-project');
    await user.type(screen.getByRole('textbox', { name: /Group ID/i }), 'com.softwaremill');

    await user.click(screen.getByRole('button', { name: /Effect type/i }));
    await user.click(screen.getByText('Future'));

    await user.click(within(screen.getByRole('radiogroup', { name: /Scala version/i })).getByText('3'));

    await user.click(
      within(
        screen.getByRole('radiogroup', {
          name: /Expose endpoint documentation using Swagger UI/i,
        })
      ).getByText('yes')
    );
    await user.click(within(screen.getByRole('radiogroup', { name: /Add JSON endpoint using/i })).getByText('circe'));

    await user.click(screen.getByRole('button', { name: /Generate .zip/i }));

    // then
    await waitFor(() => {
      expect(screen.getByText(/Server implementation must be one of the following values:/i)).toBeVisible();
    });

    expect(screen.getByRole('progressbar')).not.toBeVisible();
    expect(fetch).not.toHaveBeenCalled();
  });

  test('reset flow to form default values', async () => {
    // given
    const user = userEvent.setup();
    render(
      <BrowserRouter>
        <ConfigurationForm />
      </BrowserRouter>
    );

    // when
    await user.type(screen.getByRole('textbox', { name: /Project name/i }), 'test-project');
    await user.type(screen.getByRole('textbox', { name: /Group ID/i }), 'com.softwaremill');

    await user.click(screen.getByRole('button', { name: /Effect type/i }));
    await user.click(screen.getByText('Future'));

    await user.click(within(screen.getByRole('radiogroup', { name: /Scala version/i })).getByText('2'));

    await user.click(screen.getByRole('button', { name: /Server implementation/i }));
    await user.click(screen.getByText('Netty'));

    await user.click(within(screen.getByRole('radiogroup', { name: /Build tool/i })).getByText('Scala CLI'));

    await user.click(
      within(
        screen.getByRole('radiogroup', {
          name: /Expose endpoint documentation using Swagger UI/i,
        })
      ).getByText('yes')
    );
    await user.click(within(screen.getByRole('radiogroup', { name: /Add JSON endpoint using/i })).getByText('circe'));
    await user.click(within(screen.getByRole('radiogroup', { name: /Add metrics endpoints/i })).getByText('yes'));

    await user.click(screen.getByRole('button', { name: /Reset/i }));

    // then
    expect(screen.getByTestId('configuration-form')).toHaveFormValues({
      projectName: '',
      groupId: '',
      effect: '',
      scalaVersion: 'Scala3',
      implementation: '',
      builder: 'Sbt',
      addDocumentation: 'false',
      json: 'No',
      addMetrics: 'false',
    });
  });

  test('effect implementation field reset flow based on effect type change', async () => {
    // given
    const user = userEvent.setup();
    render(
      <BrowserRouter>
        <ConfigurationForm />
      </BrowserRouter>
    );

    // when
    await user.click(screen.getByRole('button', { name: /Effect type/i }));
    await user.click(screen.getByText('ZIO'));

    await user.click(screen.getByRole('button', { name: /Server implementation/i }));
    await user.click(screen.getByText('ZIO Http'));

    await user.click(screen.getByRole('button', { name: /Effect type/i }));
    await user.click(screen.getByText('Future'));

    // then
    expect(screen.getByTestId('configuration-form')).toHaveFormValues({
      effect: 'FutureEffect',
      scalaVersion: 'Scala3',
      implementation: '',
      // default values below
      groupId: 'com.softwaremill',
      addDocumentation: 'false',
      json: 'No',
      addMetrics: 'false',
    });
  });

  test('json field reset flow based on effect type change', async () => {
    // given
    const user = userEvent.setup();
    render(
      <BrowserRouter>
        <ConfigurationForm />
      </BrowserRouter>
    );

    // when
    await user.click(screen.getByRole('button', { name: /Effect type/i }));
    await user.click(screen.getByText('ZIO'));

    await user.click(
      within(screen.getByRole('radiogroup', { name: /Add JSON endpoint using/i })).getByText('zio-json')
    );

    await user.click(screen.getByRole('button', { name: /Effect type/i }));
    await user.click(screen.getByText('Future'));

    // then
    expect(screen.getByTestId('configuration-form')).toHaveFormValues({
      effect: 'FutureEffect',
      json: 'No',
      // default values below
      scalaVersion: 'Scala3',
      implementation: '',
      groupId: 'com.softwaremill',
      addDocumentation: 'false',
      addMetrics: 'false',
    });
  });
});
