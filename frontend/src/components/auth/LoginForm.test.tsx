// @vitest-environment jsdom

import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import LoginForm from "./LoginForm";

const loginMock = vi.fn();

vi.mock("../../hooks/useAuth", () => ({
  default: () => ({
    login: loginMock
  })
}));

vi.mock("react-router-dom", () => ({
  useNavigate: () => vi.fn()
}));

describe("LoginForm", () => {
  it("loggar in med rätt uppgifter", async () => {
    const user = userEvent.setup();

    loginMock.mockResolvedValue({ id: 1 });

    render(<LoginForm />);

    await user.type(screen.getByLabelText("E-post"), "test@test.se");

    await user.type(screen.getByLabelText("Lösenord"), "1234");

    await user.click(screen.getByRole("button", { name: "Logga in" }));

    expect(loginMock).toHaveBeenCalledWith({
      email: "test@test.se",
      password: "1234"
    });
  });
});
